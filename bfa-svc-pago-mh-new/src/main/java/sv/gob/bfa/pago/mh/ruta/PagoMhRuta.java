package sv.gob.bfa.pago.mh.ruta;

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
import sv.gob.bfa.pago.mh.model.PagoMhPeticion;
import sv.gob.bfa.pago.mh.model.PagoMhRespuesta;


public class PagoMhRuta extends RouteBuilder {
Logger logger = LoggerFactory.getLogger(PagoMhRuta.class);


@Override
public void configure() throws Exception {
	
	rutaOnServicioExceptionTimeOut();
	rutaOnServicioException();
	rutaOnJsonException();
	rutaOnGenException();
	rutaPagoMH();
	
}


public void rutaPagoMH() {
	
	from(ConfigRuta.COMPONENT_NAME+":core.servicios.pago.mh")	
	.transacted("PROPAGATION_REQUIRES_NEW")
	.routeId("core.servicios.pago.mh.activemq")
	.to("jsonval:JsonSchema/PagoMhPeticionSchema.json")
	.setHeader("peticion", simple("${body}"))
	.unmarshal().json(JsonLibrary.Jackson,PagoMhPeticion.class)
	.bean("pagoMhServicio", "procesar")
	.marshal().json(JsonLibrary.Jackson)
	.wireTap("activemq:svc.bitacora.cajas")
	;


	from(ConfigRuta.COMPONENT_NAME+":core.servicios.consulta.mh")	
	.transacted("PROPAGATION_REQUIRES_NEW")
	.routeId("core.servicios.consulta.mh.activemq")
	.setHeader("peticion", simple("${body}"))
	.unmarshal().json(JsonLibrary.Jackson,PagoMhRespuesta.class)
	.bean("consultaMhServicio", "procesar")
	.marshal().json(JsonLibrary.Jackson)
	.wireTap("activemq:svc.bitacora.cajas")
	;
		

}


//------------------------------------------------------------------------------------------------------------
//PROCESSORS
//------------------------------------------------------------------------------------------------------------




//------------------------------------------------------------------------------------------------------------
//EXCEPTION
//------------------------------------------------------------------------------------------------------------
	
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
		.wireTap("activemq:svc.bitacora.cajas");
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


public void rutaOnServicioExceptionTimeOut() {

	onException(org.apache.camel.ExchangeTimedOutException.class)
		.handled(Boolean.TRUE)
		.setProperty("codigoResp", simple("2722"))
		.bean("mensajesDeServicio","recuperarMensaje(${exchangeProperty.codigoResp}, null)")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				String msj = exchange.getIn().getBody(String.class);
				Respuesta resp = new Respuesta();
				Integer codResp = exchange.getProperty("codigoResp", Integer.class);
				resp.setCodigo(codResp);
				resp.setDescripcion(msj);
				exchange.getIn().setBody(resp);
			}
		})
		.marshal().json(JsonLibrary.Jackson)
		.wireTap("activemq:svc.bitacora.cajas")
		.markRollbackOnlyLast();
}

}

