package sv.gob.bfa.pago.gastos.ajenos.prestamo.ruta;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sv.gob.bfa.component.jsonval.JsonValidationException;
import sv.gob.bfa.core.model.Respuesta;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.pago.gastos.ajenos.prestamo.model.PagoGastosAjenosPrestamoPeticion;
import sv.gob.bfa.pago.gastos.ajenos.prestamo.model.ReversaPagoGastosAjenosPrestamoPeticion;

/**
 * Clase principal que expone los endpoints para Pago Gastos Ajenos Prestamo y su respectiva reversa.
 */
public class PagoGastosAjenosPrestamoRuta extends RouteBuilder {

	Logger logger = LoggerFactory.getLogger(PagoGastosAjenosPrestamoRuta.class);
	
	@Override
	public void configure() throws Exception {
		
		rutaOnServicioException();
		rutaOnJsonException();
		rutaOnGenException();
		rutaPagoGastosAjenosPP();
		rutaReversaPagoGastoAjenoPrestamo();
		
	}
	
	
	/**
	 * Ruta manejador de excepciones de servicio
	 */
	public void rutaOnServicioException() {
		
		onException(ServicioException.class)
			.handled(Boolean.TRUE)
			.bean("mensajesDeServicio","recuperarMensaje( ${exchangeProperty.CamelExceptionCaught.codigo}, ${exchangeProperty.CamelExceptionCaught.argumentos})")
			.process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					String msj = exchange.getIn().getBody(String.class);
					ServicioException se = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, ServicioException.class);
					Respuesta resp = new Respuesta();
					logger.error("Excepcion de servicio: {}", se.getMessage(), se);
					resp.setCodigo(se.getCodigo());
					resp.setDescripcion(msj);
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
			.bean("mensajesDeServicio","recuperarMensaje(${exchangeProperty.codigoError}, null)")
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
			.wireTap("activemq:svc.bitacora.cajas")	;
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
					String msj = exchange.getIn().getBody(String.class);
					Respuesta resp = new Respuesta();
					Integer codResp = exchange.getProperty("codigoResp", Integer.class);
					logger.error("Excepcion inesperada: {}", e.getMessage(), e);
					resp.setCodigo(codResp);
					resp.setDescripcion(msj);
					exchange.getIn().setBody(resp);
				}
			})
			.marshal().json(JsonLibrary.Jackson)
			.wireTap("activemq:svc.bitacora.cajas")
			.markRollbackOnlyLast();
	}
	
		
		/**
		 * Ruta que expone servicio que permite el registro de deposito mixto a cuenta corriente. La ruta realiza los siguientes pasos:
		 * <ul>
		 * 	<li> Consume de la cola <strong> core.servicios.depositos.cc </strong> </li>
		 *  <li> El mensaje consumido de la cola, se valida contra un Json Schema </li>
		 *  <li> Se coloca la peticion como una propiedad del objeto {@link Exchange} </li>
		 *  <li> Unmarshall hacia objeto de Dominio {@link DepositoMixtoCuentaCorrientePeticion} </li>
		 *  <li> Se invoca el servicio de la clase DepositoMixtoCuentaCorrienteServicio </li>
		 *  <li> El resultado se convierte a Json </li>
		 *  <li> La ruta finaliza </li>
		 * </ul>
		 */
		
		public void rutaPagoGastosAjenosPP() {
		//from("activemq:core.servicios.pagogastos.pp") //esta es la url, un end point a lo que es el consumidor
			from("activemq:core.servicios.pago.gastos.ajenos.prestamo")
			.transacted("PROPAGATION_REQUIRES_NEW")
			.routeId("core.servicios.pago.gastos.ajenos.prestamo.activemq")
//			.routeId("core.servicios.pago.gastos.ajenos.prestamos.direct")
			.to("jsonval:JsonSchema/PagoGastosAjenosPPPeticionSchema.json")
			.setProperty("peticion", simple("${body}"))
			.unmarshal().json(JsonLibrary.Jackson, PagoGastosAjenosPrestamoPeticion.class)
			.bean("pagoGastosAjenosPrestamo","procesar")//aqui se pone el id que pusse en el blueprint, llamar igual que en el test al hacer registro en jndi
			.marshal().json(JsonLibrary.Jackson)			
			.wireTap("activemq:svc.bitacora.cajas")//solo hace una copia del mensaje asincrono que puede continuar sin esperar que termine la operaciòn
			;
			
	
		}
		
		
		/**
		 * Ruta que expone servicio que permite la reversa de deposito mixto a cuenta corriente. La ruta realiza los siguientes pasos:
		 * <ul>
		 * 	<li> Consume de la cola <strong> core.servicios.reversa.depositos.cc </strong> </li>
		 *  <li> El mensaje consumido de la cola, se valida contra un Json Schema </li>
		 *  <li> Se coloca la peticion como una propiedad del objeto {@link Exchange} </li>
		 *  <li> Unmarshall hacia objeto de Dominio {@link ReversaPagoGastosAjenosPrestamoPeticion} </li>
		 *  <li> Se invoca el servicio de la clase ReversaDepositoMixtoCuentaCorrienteServicio </li>
		 *  <li> El resultado se convierte a Json </li>
		 *  <li> La ruta finaliza </li>
		 * </ul>
		 */
	
		public void rutaReversaPagoGastoAjenoPrestamo() {
	//		from("direct:core.servicios.reversa.pago.gastos.prestamo")
	//		.routeId("core.servicios.reversa.pago.gastos.direct")
			from("activemq:core.servicios.reversa.pago.gastos.ajenos.prestamo") //esta es la url, un end point a lo que es el consumidor
			.transacted("PROPAGATION_REQUIRES_NEW")
			.routeId("core.servicios.reversa.pago.gastos.ajenos.prestamo.activemq")
			.to("jsonval:JsonSchema/PagoGastosAjenosPPReversaPeticionSchema.json")
			.setProperty("peticion", simple("${body}"))
			.unmarshal().json(JsonLibrary.Jackson, ReversaPagoGastosAjenosPrestamoPeticion.class)
			.bean("pagoGastosAjenosPrestamoReversa","procesar")
			.marshal().json(JsonLibrary.Jackson)			
			.wireTap("activemq:svc.bitacora.cajas")
			;
			
		
		}
	}
