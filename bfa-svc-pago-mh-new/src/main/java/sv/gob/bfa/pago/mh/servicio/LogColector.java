package sv.gob.bfa.pago.mh.servicio;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import sv.gob.bfa.core.fs.FSAuxiliares;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;

public class LogColector {
	Logger logger = LoggerFactory.getLogger(LogColector.class);
	Integer colector;
    Integer    correlativo;
    FSAuxiliares fsAux;
    String dbLinkValue;
    Object peticion;
	
    private static final String INSERT_LOG_COLECTOR=" INSERT INTO CAJAS.LOG_COLECTOR ("+
																	 " ID_COLECT, " +
																	 " FECH_HORA, "  +
																	 " CORREL, " +
																	 " EVENTO, "+
																	 " DEBUG, " +
																	 " USUARIO, "+
																	 " TERMINAL, "+
																	 " FECH_REGIS, "+
																	 " OFICINA, "+
																	 " CAJA "+
															      " ) "+
															    " VALUES "+
																"(?,"+
															    " TO_TIMESTAMP (?, 'YYYY-MM-DD HH24:MI:SS.FF'), "+
																" ?,"+
															    " ?,"+
																" ?,"+
																" ?,"+
																" ?,"+
																" systimestamp,"+
																" ?,"+
																" ? "+
															    " ) ";
	 
@SuppressWarnings("unused")
public void registrar(String evento, String debug) throws ServicioException {
	         ObjectMapper oMapper = new ObjectMapper();
	         SimpleDateFormat formatEntrada = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS"); 
	       
		     
		try {
			 @SuppressWarnings("unchecked")
			Map<String, Object> map =  oMapper.convertValue(this.peticion, Map.class);
			
			 AdaptadorDeMapa adaptadorMap = null;
			 adaptadorMap = UtileriaDeDatos.adaptarMapa(map);
			 
			 
			Object[] paramsLogColector= {
				 this.colector,
				 formatEntrada.format(new Date()),
				 this.correlativo,
				 evento,
				 debug,
				 adaptadorMap.getString("codCajero"),
				 adaptadorMap.getInteger("codTerminal"),
				 adaptadorMap.getInteger("codOficinaTran"),
				 adaptadorMap.getInteger("numCaja")	 
			 };			
			
			this.fsAux.ejecutarSentencia(query(INSERT_LOG_COLECTOR), paramsLogColector, "INSERT_LOG_COLECTOR");				
			
		
		} catch (Exception e) {
			logger.error("Error inesperado en registrar en tabla de log de colectores :" + e.getMessage(), e);
			throw new ServicioException(20099, "Error");
			
		}
		
	}


protected String query(String sql) {
	return sql.replaceAll("@DBLINK@", this.dbLinkValue);
}


public LogColector(Integer colector, Integer correlativo,Object peticion, FSAuxiliares fsAux, String dbLinkValue) {
	super();
	this.colector = colector;
	this.correlativo = correlativo;
	this.fsAux = fsAux;
	this.dbLinkValue = dbLinkValue;
	this.peticion=peticion;
}

	
}
