package sv.gob.bfa.ingresocertificado.ruta;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sv.gob.bfa.component.jsonval.JsonValidationException;
import sv.gob.bfa.core.model.Respuesta;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.ingresocertificado.model.IngresoCertificadoPeticion;
import sv.gob.bfa.ingresocertificado.model.IngresoCertificadoReversaPeticion;

/**
 * Clase principal que expone los endpoints para el ingreso de certificado en caja y su respectiva reversa.
 */
public class IngresoCertificadoRuta extends RouteBuilder{
	
	Logger logger = LoggerFactory.getLogger(IngresoCertificadoRuta.class);
	
	@Override
	public void configure() throws Exception {
			
			rutaOnServicioException();
			
			rutaOnJsonException();
			
			rutaOnGenException();
		
			rutaIngresoCertificado();
			
			rutaReversaIngresoCertificado();
		
	}
	
	/**
	 * Ruta manejador de excepciones de servicio
	 */
	public void rutaOnServicioException() {
		
		onException(ServicioException.class)
			.handled(Boolean.TRUE)
			.process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					ServicioException se = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, ServicioException.class);
					Respuesta resp = new Respuesta();
					logger.error("Excepcion de servicio: {}", se.getMessage(), se);
					resp.setCodigo(se.getCodigo());
					resp.setDescripcion(se.getDescripcion());
					exchange.getIn().setBody(resp);
				}
			})
			.marshal().json(JsonLibrary.Jackson)
			.wireTap("activemq:svc.bitacora.cajas")
			.markRollbackOnlyLast()
			;
		
	}

	/**
	 * Ruta manejador de excepciones de validaciones de json
	 */
	public void rutaOnJsonException() {
		
		onException(JsonValidationException.class)
			.handled(Boolean.TRUE)
			//CODIGO CORRESPONDIENTE A PARAMETROS RECIBIDOS EN LA PETICION
			.setProperty("codigoResp", simple("20010"))
			.bean("mensajesDeServicio","recuperarMensaje(${exchangeProperty.codigoResp}, null)")
			.process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					JsonValidationException je = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, JsonValidationException.class);
					String msj = exchange.getIn().getBody(String.class);
					Respuesta resp = new Respuesta();
					Integer codResp = exchange.getProperty("codigoResp", Integer.class);
					logger.error("Excepcion de validacion de Json: {}", je.getMessage(), je);
					resp.setCodigo(codResp);
					resp.setDescripcion(msj);
					exchange.getIn().setBody(resp);
				}
			})
			.marshal().json(JsonLibrary.Jackson)
			.wireTap("activemq:svc.bitacora.cajas")
			;
		
	}
	
	/**
	 * Ruta manejador de excepciones inesperadas
	 */
	public void rutaOnGenException() {
		
		onException(Exception.class)
			.handled(Boolean.TRUE)
			.setProperty("codigoResp", simple("20001"))
			.bean("mensajesDeServicio","recuperarMensaje(${exchangeProperty.codigoResp}, null)")
			.process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
					String debug = exchange.getContext().resolvePropertyPlaceholders("{{debugEnableProperty}}");
					String msj = exchange.getIn().getBody(String.class);
					Respuesta resp = new Respuesta();
					Integer codResp = exchange.getProperty("codigoResp", Integer.class);
					logger.error("Excepcion inesperada: {}", e.getMessage(), e);
					resp.setCodigo(codResp);
					resp.setDescripcion(msj);
					//Se agrega stackTrace unicamente si la propiedad debugEnableProperty esta definida y es "true"
					logger.debug("valor debug property:" + debug);
					if (debug != null && debug.equals("true")) {
						String stackTrace = UtileriaDeDatos.obtenerStackTrace(e);
						resp.setStackTrace(stackTrace);
					}
					exchange.getIn().setBody(resp);
				}
			})
			.marshal().json(JsonLibrary.Jackson)
			.wireTap("activemq:svc.bitacora.cajas")
			.markRollbackOnlyLast()
			;
		
	}
	
	/**
	 * Ruta que expone servicio de IngresoCertificado. La ruta realiza los siguientes pasos:
	 * <ul>
	 * 	<li> Consume de la cola <strong> core.servicios.ingresocertificado </strong> </li>
	 *  <li> El mensaje consumido de la cola, se valida contra un Json Schema </li>
	 *  <li> Se coloca la peticion como una propiedad del objeto {@link Exchange} </li>
	 *  <li> Unmarshall hacia objeto de Dominio {@link IngresoCertificadoPeticion} </li>
	 *  <li> Se invoca el servicio de la clase IngresoCertificadoServicio </li>
	 *  <li> El resultado se convierte a Json </li>
	 *  <li> La ruta finaliza </li>
	 * </ul>
	 */
	public void rutaIngresoCertificado() {
		from("activemq:core.servicios.ingresocertificado")
			.transacted("PROPAGATION_REQUIRES_NEW")
			.routeId("core.servicios.ingresocertificado.activemq")
			.to("jsonval:JsonSchema/ICPeticionSchema.json")
			.setHeader("peticion", simple("${body}"))
			.unmarshal().json(JsonLibrary.Jackson, IngresoCertificadoPeticion.class)
			.bean("ingresocertificado","procesar")
			.marshal().json(JsonLibrary.Jackson)
			.wireTap("activemq:svc.bitacora.cajas")
			;
	}
	
	
	/**
	 * Ruta que expone servicio de reversa del IngresoCertificado. La ruta realiza los siguientes pasos:
	 * <ul>
	 *  <li> Consume de la cola <strong> core.servicios.reversa.ingresocertificado </strong> </li>
	 *  <li> El mensaje consumido de la cola, se valida contra un Json Schema </li>
	 *  <li> Se coloca la peticion como una propiedad del objeto {@link Exchange} </li>
	 *  <li> Unmarshall hacia objeto de Dominio {@link IngresoCertificadoReversaPeticion} </li>
	 *  <li> Se invoca el servicio de la clase IngresoCertificadoReversaServicio </li>
	 *  <li> El resultado se convierte a Json </li>
	 *  <li> La ruta finaliza </li>
	 * </ul>
	 */
	public void rutaReversaIngresoCertificado() {
		from("activemq:core.servicios.reversa.ingresocertificado")
			.transacted("PROPAGATION_REQUIRES_NEW")
			.routeId("core.servicios.reversa.ingresocertificado.activemq")
			.to("jsonval:JsonSchema/ICReversaPeticionSchema.json")
			.setHeader("peticion", simple("${body}"))
			.unmarshal().json(JsonLibrary.Jackson, IngresoCertificadoReversaPeticion.class)
			.bean("ingresocertificadoReversa","procesar")
			.marshal().json(JsonLibrary.Jackson)
			.wireTap("activemq:svc.bitacora.cajas")
			;
	}
	
}
