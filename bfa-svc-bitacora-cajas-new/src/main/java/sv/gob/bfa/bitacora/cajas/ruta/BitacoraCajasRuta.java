package sv.gob.bfa.bitacora.cajas.ruta;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import sv.gob.bfa.core.model.Peticion;
import sv.gob.bfa.core.model.Respuesta;
import sv.gob.bfa.core.util.UtileriaDeDatos;

public class BitacoraCajasRuta extends RouteBuilder{
	
	private static final String GENERATE_GLBDTIME = "sql:SELECT TRUNC(MADMIN.GENERATE_GLBDTIME_DIF, 0) AS GLBDTIME FROM DUAL";
	
	private static String INSERT_BITACORA = 
			"sql:INSERT INTO CAJAS.BITACORA(" + 
			" GLB_DTIME, COD_OFICINA, COD_TERMINAL, " + 
			" COD_CAJERO, NUM_CAJA, FEC_REAL," + 
			" FEC_SISTEMA, HORA_SISTEMA, HORA_INGRESO," + 
			" COD_RESPUESTA, PETICION, RESPUESTA)" + 
			" VALUES(" + 
			" :#${header.glbDtime}," + 
			" :#${exchangeProperty.peticionObj.codOficinaTran}, :#${exchangeProperty.peticionObj.codTerminal}, :#${exchangeProperty.peticionObj.codCajero}," + 
			" :#${exchangeProperty.peticionObj.numCaja}, :#${exchangeProperty.respuestaObj.fechaReal}, :#${exchangeProperty.respuestaObj.fechaSistema}," + 
			" :#${exchangeProperty.respuestaObj.horaSistema}, SYSDATE, :#${exchangeProperty.respuestaObj.codigo}," + 
			" :#${exchangeProperty.peticionStr}, :#${exchangeProperty.respuestaStr})"
			;
	
	private static String DATASOURCE = " ?dataSource=dataSourceCaja";
	
	Logger logger = LoggerFactory.getLogger(BitacoraCajasRuta.class);
	
	@Override
	public void configure() throws Exception {
		
		onException(Throwable.class)
			.routeId("svc.bitacora.cajas.onException")
			.log(LoggingLevel.ERROR, "ERROR AL CREAR BITACORA...")
			.process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
					logger.error("ERROR INESPERADO: {} " + ex.getMessage(), ex);
				}
			})
			.markRollbackOnlyLast()
			;
		
		from("activemq:svc.bitacora.cajas")
			.routeId("svc.bitacora.cajas.activemq")
			.transacted("PROPAGATION_REQUIRES_NEW")
			.log(LoggingLevel.INFO, "GUARDANDO BITACORA DE INVOCACION DE SERVICIO...")
			.log(LoggingLevel.DEBUG, "PETICION OBTENIDA: ${header.peticion}")
			.log(LoggingLevel.DEBUG, "BODY CONTENIDO: ${body}")
			.to(GENERATE_GLBDTIME + DATASOURCE + "&outputType=selectOne&outputHeader=glbDtime")
			.log(LoggingLevel.DEBUG, "${header.glbDtime}")
			.process(new BitacoraProcessor())
			.log(LoggingLevel.DEBUG, "INGRESANDO REGISTRO...")
			.to(INSERT_BITACORA + DATASOURCE)
			.log(LoggingLevel.INFO, "BITACORA DE INVOCACION DE SERVICIO INGRESADA...")
			;
		
	}
	
	private class BitacoraProcessor implements Processor {

		@Override
		public void process(Exchange ex) throws Exception {
			
			String peticionStr		= ex.getIn().getHeader("peticion", String.class);
			String respuestaStr	= ex.getIn().getBody(String.class);
			logger.debug("PETICION COMO STRING: {}", peticionStr);
			logger.debug("RESPUESTA COMO STRING: {}", respuestaStr);
			
			ObjectMapper mapper		= new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
			Peticion peticionObj	= mapper.readValue(peticionStr, Peticion.class);
			Respuesta respuestaObj	= mapper.readValue(respuestaStr, Respuesta.class);
			
			ex.setProperty("peticionStr", peticionStr);
			ex.setProperty("respuestaStr", respuestaStr);
			ex.setProperty("peticionObj", peticionObj);
			ex.setProperty("respuestaObj", respuestaObj);
			
		}

	}

}
