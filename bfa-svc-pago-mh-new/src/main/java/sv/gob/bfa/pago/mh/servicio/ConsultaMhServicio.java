package sv.gob.bfa.pago.mh.servicio;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.pago.mh.model.PagoMhRespuesta;


/**
 * Clase contiene logica del negocio correspondiente a Consulta Pago Ministerio de Hacienda
 */
public class ConsultaMhServicio extends Servicio{
	
	private PlatformTransactionManager platformTransactionManager;

	
	private static String SELECT_SFBDB_IEACH2 = "SELECT ICO_RESMH AS ico_resmh, IDE_RESMH AS ide_resmh, ICO_ESTAD AS ico_estad " + 
			"	FROM  LINC.SFBDB_IEACH@DBLINK@" + 
			"	WHERE  INU_REGIS = ?"
			;
	
	private static String UPDATE_SFBDB_IEACH = "UPDATE LINC.SFBDB_IEACH@DBLINK@" + 
			"	SET ICO_ESTAD = ?" +
			"	WHERE INU_REGIS = ?"			
			;
	
	private static String UPDATE_SFBDB_IEACH2 = "UPDATE LINC.SFBDB_IEACH@DBLINK@" + 
			"	SET ICO_ESTAD = ?, " +
			"       IFE_PAGO = ?, " +
			"       IHO_PAGO = ?," +
			"       IDE_RESMH = ?"+
			"	WHERE INU_REGIS = ?"			
			;
	
	Logger logger = LoggerFactory.getLogger(PagoMhServicio.class);	
	
	@Transactional("transactionManager")
	public Object procesar(Object objetoDom) throws ServicioException  {	
		
		logger.debug("AJ317 Iniciando ruta consulta MM HH ...");
		
	    DefaultTransactionDefinition paramTransactionDefinition = new    DefaultTransactionDefinition();
        TransactionStatus status=platformTransactionManager.getTransaction(paramTransactionDefinition );
        
		logger.debug("Creando objeto Datos Operacion ruta consulta ...");
		DatosOperacion datos = crearDatosOperacion();

		logger.debug("Cast de objeto de dominio -> PagoMhPeticion ruta consulta");
		PagoMhRespuesta respuesta = (PagoMhRespuesta) objetoDom;

		try {	
			
			logger.debug("Iniciando agregando propiedades del objeto al mapa ruta consulta ");
			datos.agregarPropiedadesDeObjeto(respuesta);
			datos.agregarDato("respuesta", respuesta);
			
			logger.debug("Validando respuesta de WS MM HH ruta consulta ...");
			validacionConsultaRespuestaMonitor(datos);
			
			
			respuesta = datos.obtenerObjeto("respuesta", PagoMhRespuesta.class);
			

			platformTransactionManager.commit(status);
			
			return respuesta;
		
		}catch (ServicioException e) {
			platformTransactionManager.rollback(status);
			logger.error("Ocurrio un error al consultar pago:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(e);
		} catch (TipoDatoException e) {
			platformTransactionManager.rollback(status);
			logger.error("Ocurrio un error inesperado al consultar pago:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001,"Error inesperado al consultar pago: "+e.getMessage()));
		}
		
	}
	
	/**
	 * Método para validar la respuesta obtenida del Monitor NPE, al enviar la solicitud de pago
	 * @param datos
	 * @return
	 * @throws ServicioException
	 */	
	private Integer validacionConsultaRespuestaMonitor(DatosOperacion datos) throws ServicioException {

		Integer codErr = 0;

		try {
			PagoMhRespuesta pagoMhRespuesta = datos.obtenerObjeto("respuesta", PagoMhRespuesta.class);			

			Map<String, Object> resultado = null;
			Integer ico_resmh = new Integer(0);// null;
			String ide_resmh = "";
			String ico_estad = "";
			Integer senReenvio = new Integer(0);
			Integer senRespuestaWS = new Integer(0);

			if (UtileriaDeDatos.isEquals(pagoMhRespuesta.getCodigo(), new Integer(9))) {
				logger.debug("AJ317 Expira tiempo de espera de registro, no se obtuvo respuesta pagoMhRespuesta.getCodigo() : " + pagoMhRespuesta.getCodigo() + " - " + pagoMhRespuesta.getIdTransaccionMh());
				senReenvio = Constantes.SI;
			}

			if (UtileriaDeDatos.isEquals(senReenvio, Constantes.SI)) {
				// para reenvio

				logger.debug("AJ317 Envia registro a reenvio: "  + pagoMhRespuesta.getIdTransaccionMh());
//				Object paramsIEACH_UPDATE[] = 
//					{ 
//						"RR" // AN, en power los anula comunicacion monitor - bfa no disponible
//						, pagoMhRespuesta.getIdTransaccionMh()
//						};
//				
//				logger.debug("AJ317 Ejecutando sentencia UPDATE_SFBDB_IEACH, parametros: " + paramsIEACH_UPDATE.toString());
//				ejecutarSentencia(query(UPDATE_SFBDB_IEACH), paramsIEACH_UPDATE);
				
				// monitor start sin comunicacion, monitor actualiza a RR
				// monitor stop sin comunicacion, micro deja estado en ZE

				ico_resmh = new Integer(1);
				ide_resmh = "EXITO - REENVIA";
				datos.agregarDato("ico_resmh", new Integer(1)); // si no esta el valor
				// ico_resmh y monitor deja SE
				datos.agregarDato("ide_resmh", "EXITO - REENVIA");
				
				
			} else {
				try {
					Object[] paramsIEACH2 = { 
							
							pagoMhRespuesta.getIdTransaccionMh()
							
							};
					
					logger.debug("AJ317 Ejecutando sentencia SELECT LINC SFBDB IEACH2, parametros: " + Arrays.toString(paramsIEACH2));
					resultado = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEACH2), paramsIEACH2);
				} catch (EmptyResultDataAccessException ignored) {
					logger.debug("AJ317 No existe registro sentencia SELECT LINC SFBDB IEACH2, parametros: " + pagoMhRespuesta.getIdTransaccionMh());
				}
				// respuesta de WS
				logger.debug("AJ317 Verificando si monitor cambio estado del registro:");
				if (!UtileriaDeDatos.isNull(resultado)) {
					AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(resultado);
					ico_resmh = adaptador.getInteger("ico_resmh");
					ide_resmh = adaptador.getString("ide_resmh");
					ico_estad = adaptador.getString("ico_estad"); // ESTADO SE, RE, AN, PA
					logger.debug("AJ317 Estado registro: " + pagoMhRespuesta.getIdTransaccionMh() + " codigo " + ico_resmh + " estado "+ ico_estad);
					if (UtileriaDeDatos.isEquals(ico_resmh, Constantes.SI)
							&& UtileriaDeDatos.isEquals(ico_estad, "RE")) {
						logger.debug("AJ317 Respuesta de exito recibida de WS MMHH:" + " estado " + ico_estad + " codigo " + ico_resmh + " descripcion " + ide_resmh);
//						datos.agregarDato("ico_resmh", ico_resmh);
//						datos.agregarDato("ide_resmh", ide_resmh);
						senRespuestaWS = Constantes.SI;
						//////////////
						//////////////
						datos.agregarDato("ico_resmh", ico_resmh);
						datos.agregarDato(ide_resmh, "EXITO");
						Integer fechaSistema = pagoMhRespuesta.getFechaSistema();
						Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
						Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
						Object paramsIEACH_UPDATE2[] = { 
								"PA" //
								, fechaSistemaAMD
								, pagoMhRespuesta.getHoraSistema()
								, ide_resmh
								, pagoMhRespuesta.getIdTransaccionMh()

						};
						logger.debug("AJ317 Ejecutando sentencia UPDATE_SFBDB_IEACH2, parametros: "	+ Arrays.toString(paramsIEACH_UPDATE2));
						ejecutarSentencia(query(UPDATE_SFBDB_IEACH2), paramsIEACH_UPDATE2);
						//////////////////
						/////////////////

					} else {
						if (UtileriaDeDatos.isEquals(ico_resmh, new Integer(0))
								&& UtileriaDeDatos.isEquals(ico_estad, "RE")) {
							logger.error("AJ317 Respuesta de error recibida de WS MMHH:" + " estado " + ico_estad + " codigo " + ico_resmh + " descripcion " + ide_resmh);
							datos.agregarDato("ico_resmh", ico_resmh);
							datos.agregarDato("ide_resmh", ide_resmh);
							senRespuestaWS = Constantes.SI;

						} else {
							if(UtileriaDeDatos.isEquals(ico_resmh, Constantes.SI)
									&& UtileriaDeDatos.isEquals(ico_estad, "RD")) {
								ide_resmh = "EXITO - REENVIADO";
								logger.debug("AJ317 Respuesta de exito recibida (al reestablecer comunicacion) de WS MMHH:" + " estado " + ico_estad + " codigo " + ico_resmh + " descripcion " + ide_resmh);
								senRespuestaWS = Constantes.SI;
								
								datos.agregarDato("ico_resmh", ico_resmh);
								datos.agregarDato("ide_resmh", "EXITO - REENVIADO");
							}
						}
					}
				}

				if (UtileriaDeDatos.isEquals(ico_resmh, new Integer(1)) // 
						&& UtileriaDeDatos.isEquals(senReenvio, new Integer(0))) {

//					datos.agregarDato("ico_resmh", ico_resmh);
//					datos.agregarDato(ide_resmh, "EXITO");
//					Integer fechaSistema = pagoMhRespuesta.getFechaSistema();
//					Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
//					Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
//					Object paramsIEACH_UPDATE2[] = { 
//							"PA" //
//							, fechaSistemaAMD
//							, pagoMhRespuesta.getHoraSistema()
//							, ide_resmh
//							, pagoMhRespuesta.getIdTransaccionMh()
//
//					};
//					logger.debug("AJ317 Ejecutando sentencia UPDATE_SFBDB_IEACH2, parametros: "	+ paramsIEACH_UPDATE2.toString());
//					ejecutarSentencia(query(UPDATE_SFBDB_IEACH2), paramsIEACH_UPDATE2);
				} else {

//					datos.agregarDato("ico_resmh", ico_resmh);
//					datos.agregarDato("ide_resmh", ide_resmh);

				}

			}

			datos.agregarDato("codErr", codErr); // FIXME

			logger.error("AJ317 Preparando respuesta de ruta consulta, valida reenvio o respuesta de WS MMHH:");
			logger.error("AJ317 Preparando respuesta de ruta consulta:" + " codigo " + ico_resmh + " estado " + ico_estad + " descripcion " + ide_resmh +  " senRespuestaWS " + senRespuestaWS );
			if (UtileriaDeDatos.isEquals(ico_resmh, new Integer(1))) { // EXPIRO ESPERA, MONITOR ARRIBA SIN COMUNICACION,
				pagoMhRespuesta.setCodigo(new Integer(0));  //  REENVIO 1, PARA MICRO EXITO 0 SE CAMBIA
				pagoMhRespuesta.setDescripcion(ide_resmh);
			} else {
				if (UtileriaDeDatos.isEquals(ico_resmh, new Integer(0))) { // MONITOR ARRIBA SIN COMUNICACION , MONITOR CAMBIA ESTADO A RR, CODIGO 0
//					pagoMhRespuesta.setCodigo(new Integer(1));             // VENTANILLA VALIDA 1, ESTADO DIFF. A ZE, LANZA ERR
					if(UtileriaDeDatos.isEquals(senRespuestaWS, Constantes.SI)) {
						logger.error("AJ317 Ocurrio un error respuesta WS MMHH:" + " registro " + pagoMhRespuesta.getIdTransaccionMh() + " estado " + ico_estad + " codigo " + ico_resmh + " descripcion " + ide_resmh);
						pagoMhRespuesta.setCodigo(new Integer(1));
						pagoMhRespuesta.setDescripcion(ide_resmh);
						logger.error("AJ317 Ocurrio un error respuesta WS MMHH ruta consulta retorna respuesta:" + " codigo: 1 " + " descripcion " + ide_resmh);
					}else {
//						pagoMhRespuesta.setDescripcion(ico_estad); // SE
						if(UtileriaDeDatos.isEquals(senRespuestaWS, new Integer(0))) { // SIN EXPIRAR TIEMPO
							logger.debug("AJ317 No se obtuvo respuesta WS MMHH:" + " registro " + pagoMhRespuesta.getIdTransaccionMh() + " estado " + ico_estad + " codigo " + ico_resmh + " descripcion " + ide_resmh);
							pagoMhRespuesta.setCodigo(new Integer(2));
							pagoMhRespuesta.setDescripcion("ZE");		// ZE MONITOR ABAJO, RR MONITOR ARRIBA SIN COMUNICACION
							logger.debug("AJ317 No se obtuvo respuesta WS MMHH ruta consulta retorna respuesta:" + " codigo: 2 " + " descripcion ZE");
						}
					}
					
				}
			}
			datos.agregarDato("pagoMhRespuesta", pagoMhRespuesta);
			return codErr;

		} catch (TipoDatoException | ParseException e) {
			logger.error("Ocurrio un error inesperado al verificar pago en ruta consulta:", e.getMessage(), e);
			throw new ServicioException(20001, "Error inesperado al verificar pago: " + e.getMessage());
		}

	}
   	
	public PlatformTransactionManager getPlatformTransactionManager() {
		return platformTransactionManager;
	}

	public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
		this.platformTransactionManager = platformTransactionManager;
	}	


}
