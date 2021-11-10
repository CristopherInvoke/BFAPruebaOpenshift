package sv.gob.bfa.deposito.mixto.cuentaahorro.ruta;

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
import sv.gob.bfa.deposito.mixto.cuentaahorro.model.DepositoMixtoCuentaAhorroPeticion;
import sv.gob.bfa.deposito.mixto.cuentaahorro.model.ReversaDepositoMixtoCuentaAhorroPeticion;

public class DepositoMixtoCuentaAhorroRuta extends RouteBuilder{

	Logger logger = LoggerFactory.getLogger(DepositoMixtoCuentaAhorroRuta.class);
	
	@Override
	public void configure() throws Exception {
		
		rutaOnServicioException();
		rutaOnJsonException();
		rutaOnGenException();
		rutaDepositoMixtoAH();
		rutaReversaaDepositoMixtoAH();
		
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
					String debug = exchange.getContext().resolvePropertyPlaceholders("{{debugEnableProperty}}");
					Respuesta resp = new Respuesta();
					logger.error("Excepcion de servicio: {}", se.getMessage(), se);
					resp.setCodigo(se.getCodigo());
					resp.setDescripcion(se.getDescripcion());
					
					//Se agrega stackTrace unicamente si la propiedad debugEnableProperty esta definida y es "true"
					logger.debug("valor debug property:" + debug);
					if (debug != null && debug.equals("true")) {
						String stackTrace = UtileriaDeDatos.obtenerStackTrace(se);
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
	 * Ruta que expone servicio que permite el registro de deposito mixto a cuenta de ahorro. La ruta realiza los siguientes pasos:
	 * <ul>
	 * 	<li> Consume de la cola <strong> core.servicios.depositos.ah </strong> </li>
	 *  <li> El mensaje consumido de la cola, se valida contra un Json Schema </li>
	 *  <li> Se coloca la peticion como una propiedad del objeto {@link Exchange} </li>
	 *  <li> Unmarshall hacia objeto de Dominio {@link DepositoMixtoCuentaAhorroPeticion} </li>
	 *  <li> Se invoca el servicio de la clase DepositoMixtoCuentaAhorroServicio </li>
	 *  <li> El resultado se convierte a Json </li>
	 *  <li> La ruta finaliza </li>
	 * </ul>
	 */
	
	public void rutaDepositoMixtoAH() {
	from("activemq:core.servicios.depositos.ah")
		.transacted("PROPAGATION_REQUIRES_NEW")
		.routeId("core.servicios.depositos.ah.activemq")
		.to("jsonval:JsonSchema/DepMixtoAHPeticionSchema.json")
		.setHeader("peticion", simple("${body}"))
		.unmarshal().json(JsonLibrary.Jackson, DepositoMixtoCuentaAhorroPeticion.class)
		.bean("depositoMixtoCuentaAhorroServicio","procesar")
		.marshal().json(JsonLibrary.Jackson)
		.wireTap("activemq:svc.bitacora.cajas")
		;
	
	}
	
	/**
	 * Ruta que expone servicio que permite la reversa de deposito mixto a cuenta de ahorro. La ruta realiza los siguientes pasos:
	 * <ul>
	 * 	<li> Consume de la cola <strong> core.servicios.depositos.ah </strong> </li>
	 *  <li> El mensaje consumido de la cola, se valida contra un Json Schema </li>
	 *  <li> Se coloca la peticion como una propiedad del objeto {@link Exchange} </li>
	 *  <li> Unmarshall hacia objeto de Dominio {@link DepositoMixtoCuentaAhorroPeticion} </li>
	 *  <li> Se invoca el servicio de la clase DepositoMixtoCuentaAhorroServicio </li>
	 *  <li> El resultado se convierte a Json </li>
	 *  <li> La ruta finaliza </li>
	 * </ul>
	 */
	public void rutaReversaaDepositoMixtoAH() {
	from("activemq:core.servicios.reversa.depositos.ah")
		.transacted("PROPAGATION_REQUIRES_NEW")
		.routeId("core.servicios.reversa.depositos.ah.activemq")
		.to("jsonval:JsonSchema/DepMixtoAHReversaPeticionSchema.json")
		.setHeader("peticion", simple("${body}"))
		.unmarshal().json(JsonLibrary.Jackson, ReversaDepositoMixtoCuentaAhorroPeticion.class)
		.bean("reversaDepositoMixtoCuentaAhorroServicio","procesar")
		.marshal().json(JsonLibrary.Jackson)
		.wireTap("activemq:svc.bitacora.cajas")
		;
	
	}

}
