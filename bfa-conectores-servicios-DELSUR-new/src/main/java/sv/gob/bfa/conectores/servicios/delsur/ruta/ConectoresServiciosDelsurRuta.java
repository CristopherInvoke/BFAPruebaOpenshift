package sv.gob.bfa.conectores.servicios.delsur.ruta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.cxf.message.MessageContentsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import sv.gob.bfa.conectores.servicio.delsur.dto.ConectoresServiciosDelsurPeticion;
import sv.gob.bfa.conectores.servicio.delsur.dto.ConectoresServiciosDelsurRespuesta;
import sv.gob.bfa.soporte.comunes.exception.ServiceException;

public class ConectoresServiciosDelsurRuta extends RouteBuilder {

	//Banderas de ayuda
	
	private static final String OPERACION_CONSULTA_PAGO = "Consultar_Saldo";
	private static final String OPERACION_APLICAR_PAGO = "Aplicar_Pago";
	private static final String OPERACION_ANULAR_PAGO = "Anular_Pago";
	
	// String que configura la ruta de cxf a invocar
	
	private static final String SERVICE_CLASS = "?serviceClass=sv.delsur.colectores.erp.pagolinea.AplicarPago";
	
	Logger logger = LoggerFactory.getLogger(ConectoresServiciosDelsurRuta.class);
	/*:::Conectere DELSUR:::*/
	@Override
	public void configure() throws Exception {
		logger.info("Iniciando configuracion");
		onException(Exception.class)
			.routeId("conectores.servicios.delsur.exception.handler")
			.handled(true)
			.log(LoggingLevel.ERROR,"Ocurrio un error: ${exception.message}")
//			.process("exceptionProcessor")
//			.choice()
//				.when(simple("${header.tipoRespuesta} == 'jms'"))
//					.marshal().json(JsonLibrary.Jackson)
//			.end()
			;

		from("direct:conectores.servicios.delsur")
			.routeId("conectores.servicios.delsur.direct")
			.log(LoggingLevel.DEBUG, "====== PETICION RECIBIDA ======== ${exchangeProperty.originalRequest}")
			.log(LoggingLevel.DEBUG,"INICIANDO RUTA CONECTOR DELSUR...")
			.process(new ProcesarValidarPeticion())
			.log(LoggingLevel.DEBUG,"INICIANDO CONSULTA DE WS...")
			//TODO esto se debe cambiar por variable 
			//Invocando el servicio
			//.to("cxf:bean:delsurCXF?continuationTimeout={{activemq.requestTimeoutDELSUR}}")
			.to("cxf:bean:delsurCXF?continuationTimeout=70s")
			.log(LoggingLevel.DEBUG,"PROCESANDO RESPUESTA DE WS...")
			//Creando respuesta final
			.log(LoggingLevel.INFO,"process respuesta")
			.process(new ProcesarRespuesta())
			.log(LoggingLevel.INFO,"FINALIZANDO RUTA CONECTOR DELSUR...")
			;
		
		from("activemq:conectores.servicios.delsur")
			.routeId("conectores.servicios.delsur.activemq")
			.unmarshal().json(JsonLibrary.Jackson,ConectoresServiciosDelsurPeticion.class)
			.setProperty("originalRequest",simple("${body}"))
			.to("direct:conectores.servicios.delsur")
			.log(LoggingLevel.INFO,"Finalizando direct")
			.marshal().json(JsonLibrary.Jackson);
		
	}
	
	
	//----------------------------------------------------------------------------------------------------
	//PROCESSORS
	
	private class ProcesarValidarPeticion implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {
			logger.info("processor");
			//Validar parametros segun peticion
			
			
			ConectoresServiciosDelsurPeticion peticion = (ConectoresServiciosDelsurPeticion) exchange.getProperty("originalRequest");
			logger.info("processor 1");
			ObjectMapper mapper = new ObjectMapper();
			logger.debug("===== CREANDO PETICION PARA WS DESDE CONECTOR ======= {}", mapper.writeValueAsString(peticion));
			logger.info("processor 2.1");
			if(peticion == null
					|| ( peticion.getMetodo() == null || "".equals(peticion.getMetodo()))
					|| ((peticion.getNpe() == null || "".equals(peticion.getNpe()))
							&& ( peticion.getNis() == null || "".equals(peticion.getNis()))))
						throw new ServiceException(10, "Datos incompletos para realizar operacion.");
			logger.info("processor 2.2");
			if(OPERACION_APLICAR_PAGO.equals(peticion.getMetodo())
				&& (peticion.getReferenciaDELSUR() == null || "".equals(peticion.getReferenciaDELSUR()))
				&& (peticion.getCodAgencia() == null || "".equals(peticion.getCodAgencia()))
				&& (peticion.getCodSucursal() == null || "".equals(peticion.getCodSucursal()))
				&& (peticion.getPagoAlcaldia() == null)
				&& (peticion.getMonto() == null )
				)  throw new ServiceException(10, "Datos incompletos para realizar operacion.");
			logger.info("processor 2.3");
			if(OPERACION_ANULAR_PAGO.equals(peticion.getMetodo())
					&& (peticion.getReferenciaDELSUR() == null || "".equals(peticion.getReferenciaDELSUR()))
					&& (peticion.getCodAgencia() == null || "".equals(peticion.getCodAgencia()))
					&& (peticion.getCodSucursal() == null || "".equals(peticion.getCodSucursal()))
					)  throw new ServiceException(10, "Datos incompletos para realizar operacion.");
			
			
			//Preparar petición
			
			Object wsPeticion = null;
			
			if(OPERACION_CONSULTA_PAGO.equals(peticion.getMetodo())) {

				String valor = "";
				
				if(peticion.getNpe() != null && !"".equals(peticion.getNpe()))
					valor = peticion.getNpe();
				else valor = peticion.getNis();
				
				wsPeticion = new Object[] {valor};
				logger.info("processor 2.4");
			}
			else if(OPERACION_APLICAR_PAGO.equals(peticion.getMetodo())){
				
				BigDecimal monto = peticion.getMonto().setScale(2, RoundingMode.HALF_UP);
				
				wsPeticion = new Object[] {Long.parseLong(peticion.getReferenciaDELSUR()),
						Long.parseLong(peticion.getNis()),
						Long.parseLong(peticion.getCodAgencia()),
						Long.parseLong(peticion.getCodSucursal()),
						Integer.toString(peticion.getPagoAlcaldia()),
						monto.doubleValue()};
				logger.info("processor 2.5");
			}
			else if(OPERACION_ANULAR_PAGO.equals(peticion.getMetodo())){
				wsPeticion = new Object[] {Long.parseLong(peticion.getReferenciaDELSUR()),
						Long.parseLong(peticion.getNis()),
						Long.parseLong(peticion.getCodAgencia()),
						Long.parseLong(peticion.getCodSucursal())};
				logger.info("processor 2.6");
				}
			
			if(wsPeticion == null) throw new ServiceException(1, "Ocurrio un error inesperado.");
			logger.info("processor 2.7");
			//Colocar encabezado
			exchange.getIn().setHeader(CxfConstants.OPERATION_NAME, peticion.getMetodo());
             
			//Creando peticion
			logger.debug("======= PETICION ENVIADA AL WS DESDE CONECTOR ====== {}", wsPeticion);
			exchange.getIn().setBody(wsPeticion);
			logger.info("processor 2.8");
			
		}
	}
	
	private class ProcesarRespuesta implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {
			System.out.println("Respuesta WS");
			// TODO Crear proceso para recibir la respuesta
			ObjectMapper mapper = new ObjectMapper();
			ConectoresServiciosDelsurPeticion peticion = (ConectoresServiciosDelsurPeticion) exchange.getProperty("originalRequest");
			
			ConectoresServiciosDelsurRespuesta respuesta = new ConectoresServiciosDelsurRespuesta();
			
			logger.info("=====Recuperando respuesta del WS=====");
			System.out.println("Dentro de proceso respuesta");
			Message in = exchange.getIn();
			
			
			if(in.getBody() == null ) throw new ServiceException(1, "No se obtuvo respuesta del servicio.");
			
			MessageContentsList listaResultado = in.getBody(MessageContentsList.class);
			
			for(Object resultado: listaResultado) {
				
				HashMap<String, Object> mensaje = new ObjectMapper().readValue((String) resultado,HashMap.class);
				logger.debug("===== RESPUESTA WS ===== " + mensaje.toString());
				respuesta.setCodigo(Integer.parseInt((String) mensaje.get("CodError")));
				respuesta.setDescripcion((String) mensaje.get("DescError"));
				
				if(respuesta.getCodigo() == 0 && OPERACION_CONSULTA_PAGO.equals(peticion.getMetodo()) ) {
					
					respuesta.setReferenciaDELSUR((String) mensaje.get("RefDelsur"));
					respuesta.setNis((String) mensaje.get("Nis"));
					respuesta.setNombreCliente((String) mensaje.get("NomCliente"));
					
					//Transforma valores para ser utilizados como numeros
					String montoEnergia = ((String) mensaje.get("MontoEnergia")).trim();
					String montoAlcaldia = ((String) mensaje.get("MontoAlcaldia")).trim();
					String montoReconexion = ((String) mensaje.get("MontoReconexion")).trim();
					
					respuesta.setMontoEnergia(new BigDecimal(montoEnergia.replaceAll(",","")));
					respuesta.setMontoAlcaldia(new BigDecimal(montoAlcaldia.replaceAll(",","")));
					respuesta.setMontoReconexion(new BigDecimal(montoReconexion.replaceAll(",","")));
				}
				else if(respuesta.getCodigo() == 0) {
					respuesta.setReferenciaDELSUR((String) mensaje.get("RefDelsur"));
				}
			}
			
			logger.debug("======== RESPUESTA DE CONECTOR DELSUR ========= {}", mapper.writeValueAsString(respuesta));
			exchange.getIn().setBody(respuesta);
		}
	}

}
