package sv.gob.bfa.pagocolectorgenerico.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;

import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.model.CuentaAhorro;
import sv.gob.bfa.core.model.CuentaCorriente;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.pagocolectorgenerico.model.ReversaPagoColectorGenericoPeticion;
import sv.gob.bfa.pagocolectorgenerico.model.ReversaPagoColectorGenericoRespuesta;

public class ReversaPagoColectorGenericoServicio extends Servicio{

	private static final String NOM_COD_SERVICIO = "Reversa pago colector generico AJ499: ";
	
	private static final String SELECT_LINC_SFBDB_IEMCO	= 
			" SELECT SCO_IDENT AS codClienteIEMCO" + 
			" FROM   LINC.SFBDB_IEMCO@DBLINK@" + 
			" WHERE  ICO_COLECT = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_ICRDO = 
			"SELECT	ACO_CAUSA AS codCausal," + 
			"		ACOSUBCAU AS codSubCausal," + 
			"		ISEVALREG AS senValidacionRegistro," + 
			"		ACOTIPCOR AS codTipoCorrelativo," + 
			"		IDEDOCUM AS descTipoDocumento" + 
			"	FROM LINC.SFBDB_ICRDO@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?";
	
	private static final String SELECT_LINC_SFBDB_AAMTM = 
			"SELECT ASESUBCAU AS senSubCausal" + 
			"	FROM  LINC.SFBDB_AAMTM@DBLINK@" + 
			"	WHERE	DCO_ISPEC = ?" + 
			"		AND ACO_CAUSA = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_AAMSM = 
			"SELECT ACU_PRODU AS codProductoCta," + 
			"		ACU_OFICI AS codOficinaCta," + 
			"		ACUNUMCUE AS numCuenta," + 
			"		ACUDIGVER AS digitoVerificadorCta," + 
			"		LPAD(ACU_PRODU, 3, 0) || LPAD(ACU_OFICI, 3, 0) || LPAD(ACUNUMCUE, 6, 0) || ACUDIGVER AS cuentaStr" + 
			"	FROM LINC.SFBDB_AAMSM@DBLINK@" + 
			"	WHERE	DCO_ISPEC = ?" + 
			"		AND ACO_CAUSA = ?" + 
			"		AND ACOSUBCAU = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_IEAGE = 
			"SELECT INO_CAMPO AS nombreCampo," + 
			"		ISEVISIBLE AS senVisible," + 
			"		INU_LARGO AS longitudCampo," + 
			"		ISE_OBLIGA AS senObligatorio," + 
			"   	ISE_ARCHI AS senArchivo" + 
			"	FROM LINC.SFBDB_IEAGE@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"   	AND INU_CAMPO > ?" + 
			"		ORDER BY INU_CAMPO"
			;
	
	private static final String SELECT_LINC_SFBDB_ICATR = 
			"SELECT SUM(IMO_PAGO) AS montoPago" + 
			"	FROM LINC.SFBDB_ICATR@DBLINK@" + 
			"	WHERE	IFE_TRANS = ?" + 
			"		AND DCO_OFICI = ?" + 
			"		AND DCO_TERMI = ?" + 
			"		AND DCO_USUAR = ?" + 
			"		AND INU_REGIS > ? " + 
			"		AND TSE_REVER != ?" + 
			"		AND INUREGCLI = ?" + 
			"		AND TNUDOCTRA = ?" + 
			"		AND IMO_PAGO  = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_AAATR = 
			"SELECT GLB_DTIME as glbDtimeAAATR" + 
			"	FROM LINC.SFBDB_AAATR@DBLINK@" + 
			"   WHERE	TFETRAREL = ?" + 
			"		AND DCO_OFICI = ?" + 
			"		AND DCO_TERMI = ?" + 
			"		AND TNU_TRANS = ?" + 
			"		AND DCO_TRANS = ?" + 
			"		AND ACO_CAUSA = ?" + 
			"		AND ACOSUBCAU = ?" + 
			"		AND TNUDOCTRA = ?" + 
			"		AND TVA_MOVIM = ?" + 
			"		AND TSE_REVER != ?"
			;
	
	private static final String SELECT_LINC_SFBDB_ICPGE = 
			"SELECT INO_CLIEN AS nomCliente," + 
			"		INUREGCLI AS numRegistroCliente," + 
			"		ICO_ESTAD AS codEstado," + 
			"		IFE_PAGO AS fechaPago," + 
			"		IVA_PAGO AS valorPago," + 
			"		ICOTIPCLI AS tipoCliente," + 
			"		IFE_CARGA AS fechaCarga," + 
			"		IHO_CARGA AS horaCarga" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND ICOTIPDOC  = ?" + 
			"		AND INUREGCLI  = ?" +
			"       AND IFE_CARGA  = ?"
			;
	
	private static final String SELECT_SFBFB_ICPGE2 =
			"SELECT MAX(IFE_CARGA)" +
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND ICOTIPDOC  = ?" + 
			"		AND INUREGCLI  = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_ICPGE_TOTAL = 
			"SELECT SUM(IVA_PAGO) AS totalPlanICPGE" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?" + 
			"		AND IFE_PAGO  = ?" + 
			"		AND INU_DOCUM = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_AAATR = 
			"UPDATE LINC.SFBDB_AAATR@DBLINK@" + 
			"	SET TSE_REVER = ?" + 
			"	WHERE	TFETRAREL  = ?" + 
			"		AND DCO_OFICI  = ?" + 
			"		AND DCO_TERMI  = ?" + 
			"		AND TNU_TRANS  = ?" + 
			"		AND DCO_TRANS  = ?" + 
			"		AND ACO_CAUSA  = ?" + 
			"		AND ACOSUBCAU  = ?" + 
			"		AND TNUDOCTRA  = ?" + 
			"		AND TVA_MOVIM  = ?" + 
			"		AND TSE_REVER  != ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_ICPGE = 
			"UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET IFE_PAGO = ?," + 
			"		ICO_ESTAD = ?" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND ICOTIPDOC  = ?" + 
			"		AND INUREGCLI  = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_ICATR = 
			"UPDATE LINC.SFBDB_ICATR@DBLINK@" + 
			"	SET TSE_REVER = ?" + 
			"	WHERE	IFE_TRANS  = ?" + 
			"		AND DCO_OFICI  = ?" + 
			"		AND DCO_TERMI  = ?" + 
			"		AND DCO_USUAR  = ?" + 
			"		AND INU_REGIS  > ?" + 
			"		AND INUREGCLI  = ?" + 
			"		AND TNUDOCTRA  = ?" + 
			"		AND IMO_PAGO   = ?" + 
			"		AND TSE_REVER != ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_ICPGE_1 = 
			"UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET IFE_PAGO = ?," + 
			"		ICO_ESTAD = ?" + 
			"	WHERE	ICO_COLECT = ?" + 
			"   	AND ICOTIPDOC  = ?" + 
			"   	AND IFE_PAGO   = ?" + 
			"  		AND INU_DOCUM  = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_ICATR_1 = 
			"UPDATE LINC.SFBDB_ICATR@DBLINK@" + 
			"	SET TSE_REVER = ?" + 
			"	WHERE	IFE_TRANS = ?" + 
			"		AND DCO_OFICI = ?" + 
			"		AND DCO_TERMI = ?" + 
			"		AND DCO_USUAR = ?" + 
			"		AND INU_REGIS > ?" + 
			"		AND INUREGCLI = ?" + 
			"		AND TNUDOCTRA = ?" + 
			"		AND TSE_REVER != ?"
			;
	
	private static final String SELECT_LINC_SFBDB_ICATR_SIN_PLANILLA = 
			"SELECT GLB_DTIME as glbDtimeICATR" + 
			"	FROM LINC.SFBDB_ICATR@DBLINK@" + 
			"	WHERE	IFE_TRANS  = ?" + 
			"		AND DCO_OFICI  = ?" + 
			"		AND DCO_TERMI  = ?" + 
			"		AND DCO_USUAR  = ?" + 
			"		AND INU_REGIS  > ?" + 
			"		AND INUREGCLI  = ?" + 
			"		AND TNUDOCTRA  = ?" + 
			"		AND IMO_PAGO   = ?" + 
			"		AND TSE_REVER != ?"
			;
	
	private static final String SELECT_LINC_SFBDB_ICATR_CON_PLANILLA = 
			"SELECT GLB_DTIME as glbDtimeICATR" + 
			"	FROM LINC.SFBDB_ICATR@DBLINK@" + 
			"	WHERE	IFE_TRANS = ?" + 
			"		AND DCO_OFICI = ?" + 
			"		AND DCO_TERMI = ?" + 
			"		AND DCO_USUAR = ?" + 
			"		AND INU_REGIS > ?" + 
			"		AND INUREGCLI = ?" + 
			"		AND TNUDOCTRA = ?" + 
			"		AND TSE_REVER != ?"
			;
	
	private static String DELETE_SFBDB_P_ICATR = "DELETE FROM LINC.SFBDB_P_ICATR@DBLINK@" + 
			  " WHERE TO_CHAR(GLB_DTIME) = ? "
			 ;
	
	private static String DELETE_SFBDB_P_ICATR01 = "DELETE FROM  LINC.SFBDB_P_ICATR01@DBLINK@" + 
			  " WHERE TO_CHAR(GLB_DTIME) = ? "
			 ;
	
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");

		try {
			
			logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion ...");
			DatosOperacion datos = crearDatosOperacion();
			
			logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> ReversaPagoColectorGenericoPeticion");
			ReversaPagoColectorGenericoPeticion peticion = (ReversaPagoColectorGenericoPeticion) objetoDom;
			datos.agregarDato("peticion", peticion);
			//1.
			validadacionInicial(peticion);
			String numDocumentoTranAux = String.valueOf(peticion.getNumDocumentoTran());		
			if(UtileriaDeDatos.isGreater(numDocumentoTranAux.length(), new Integer(8)) && UtileriaDeDatos.isEquals(peticion.getCodColector(), new Integer(148))) {
				peticion.setNumDocumentoTran(Integer.valueOf(numDocumentoTranAux.substring(0, 8)));			
			}	
			
			//2.
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("coTerminal", peticion.getCodTerminal());
			datos.agregarDato("nuReversa", peticion.getNumReversa());
			
			seguridadTerminalesFinancieros(datos);
			
			//Validaciones 3. 4. 5. y 6.
			validaciones(datos);
			
			//7.
			obtenerValidarColectores(datos);
			
			//8. y 9.
			validarNumDocValMovimiento(datos);
			
			//10.
			validarParametrosRegistro(datos);
			
			//Preparacion de objeto respuesta del servicio.
			ReversaPagoColectorGenericoRespuesta respuesta = new ReversaPagoColectorGenericoRespuesta();
			datos.llenarObjeto(respuesta);
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			return respuesta;
		} catch (ServicioException se) {
			logger.error("Ocurrio un error:", se.getMessage(), se);
			throw manejarMensajeExcepcionServicio(se);
		} catch (TipoDatoException | ParseException tde) {
			logger.error("Ocurrio un error inesperado:", tde.getMessage(), tde);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + tde.getMessage()));
		}
	}
	
	private void validarParametrosRegistro(DatosOperacion datos) throws TipoDatoException, ServicioException, ParseException {
		//10.
		ReversaPagoColectorGenericoPeticion peticion	= datos.obtenerObjeto("peticion", ReversaPagoColectorGenericoPeticion.class);
		AdaptadorDeMapa relColectorDocumento			= datos.obtenerObjeto("relColectorDocumento", AdaptadorDeMapa.class);
		Integer senValidacionRegistro					= datos.obtenerInteger("senValidacionRegistro");
		
		if (UtileriaDeDatos.isGreater(senValidacionRegistro, Constantes.GC_PAGO_DIFERENTE_CUOTA) || 
			UtileriaDeDatos.isEquals(senValidacionRegistro, new Integer(0))) {
			throw new ServicioException(20018, "VALOR INCORRECTO {}","PARAMETRO DE VALIDACION DE REGISTRO");
		}
		
		Integer fechaRelativa = datos.obtenerInteger("fechaRelativa");
		Long result = null;
		try {
			
			Object[] paramsAAATR = {
				fechaRelativa, peticion.getCodOficinaTran(), peticion.getCodTerminal(), peticion.getNumReversa(),
				peticion.getCodTran(), relColectorDocumento.getInteger("codCausal"), relColectorDocumento.getInteger("codSubCausal"), 
				peticion.getNumDocumentoTran(), peticion.getValorMovimiento().setScale(2, BigDecimal.ROUND_UP), Constantes.SI
			};
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAATR, parametro: {}", Arrays.toString(paramsAAATR));
			result = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_AAATR), Long.class, paramsAAATR);
			
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "No existe la operacion realizada.");
			throw new ServicioException(20212, "No existe la operacion realizada.");
		}
		
		Integer numRegistroCliente	= new Integer(0);
		Integer senPlanilla			= datos.obtenerInteger("senPlanilla");
		String nomCliente = "";
		String codEstado = "";
		Integer fechaPago = null;
		BigDecimal valorPago = null;
		Integer tipoClienteICPGE = null;
		Integer fechaCarga = null;
		Integer horaCarga = null;
		Integer fechaVencimiento = 0;
		AdaptadorDeMapa infoCliente = null;
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO)) {
			
			if (UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_CUOTA_EXACTA) || 
				UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_MAYOR_CUOTA) || 
				UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_DIFERENTE_CUOTA)
				) {
				
				try {
					
					Object[] paramsICPGE = {
						peticion.getCodColector(),
						peticion.getCodTipoDocumento(),
						datos.obtenerValor("numCredito")
					};
					
					Integer IFE_CARGA = 0;
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE2, parametros: {}", Arrays.toString(paramsICPGE));
					IFE_CARGA = jdbcTemplate.queryForObject(query(SELECT_SFBFB_ICPGE2), Integer.class, paramsICPGE);
					
					Object[] paramsICPGE2 = {
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							datos.obtenerValor("numCredito"),
							IFE_CARGA,
						};
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE2));
					Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_ICPGE), paramsICPGE2);
					infoCliente	= UtileriaDeDatos.adaptarMapa(queryForMap);
					
					nomCliente = infoCliente.getString("nomCliente");
					numRegistroCliente = infoCliente.getInteger("numRegistroCliente");
					codEstado =  infoCliente.getString("codEstado");
					fechaPago =  infoCliente.getInteger("fechaPago");
					valorPago =  infoCliente.getBigDecimal("valorPago");
					tipoClienteICPGE =  infoCliente.getInteger("tipoClienteICPGE");
					fechaCarga =  infoCliente.getInteger("fechaCarga");
					horaCarga = infoCliente.getInteger("horaCarga");
					
					datos.agregarDato("fechaCarga", fechaCarga);
					datos.agregarDato("horaCarga", horaCarga);
					datos.agregarDato("nomCliente", nomCliente);
					
				} catch (EmptyResultDataAccessException erdae) {
					logger.error(NOM_COD_SERVICIO + "NO EXISTE REGISTRO DE CUOTA BASE-COLECTOR");
					throw new ServicioException(20019, "No existe {} ", " REGISTRO DE CUOTA BASE-COLECTOR");
				}
				
			}
			
			if (
				(UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_CUOTA_EXACTA) || 
				UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_MAYOR_CUOTA) || 
				UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_DIFERENTE_CUOTA)
				) &&
				(
				!UtileriaDeDatos.isEquals(infoCliente.getString("codEstado"), "P") || 
				UtileriaDeDatos.isEquals(infoCliente.getInteger("fechaPago"), new Integer(0))
				)
				) {
				throw new ServicioException(20016, "Estado Incorrecto {} ", " DEL REGISTRO PARA REVERSION") ;
			}
			
			try {
				//numCredito ya esta
				datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
				datos.agregarDato("montoPago", peticion.getValorMovimiento());
				transaccionesColector(datos.obtenerString("numCredito"), datos);
			} catch (EmptyResultDataAccessException e) {
				logger.error(NOM_COD_SERVICIO + "No existe registro de cuota para reversion. {}" + e.getMessage(), e);
				throw new ServicioException(20019, "No existe {} ", "REGISTRO DE CUOTA PARA REVERSION");
			}
			
		}
		
		BigDecimal totalPlanICPGE = null;
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.SI)) {
			if (UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_CUOTA_EXACTA) || 
					UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_MAYOR_CUOTA) || 
					UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_DIFERENTE_CUOTA)
					) {
					
					try {
						
						Object[] paramsICPGE = {
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							datos.obtenerInteger("fechaSistemaAMD"),
							" "
						};
						logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}" + Arrays.toString(paramsICPGE));
						totalPlanICPGE = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_ICPGE_TOTAL), BigDecimal.class, 
																							paramsICPGE);
						
						datos.agregarDato("totalPlanICPGE", totalPlanICPGE);
						
					} catch (EmptyResultDataAccessException erdae) {
						logger.error(NOM_COD_SERVICIO + "ARCHIVO PAGADO PARA REVERSIÓN");
						throw new ServicioException(20019, "No existe {} ", "ARCHIVO PAGADO PARA REVERSIÓN");
					}
					
				}
			
			if (!UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), totalPlanICPGE)) {
				throw new ServicioException(20224, "VALOR DE LA TRANSACCION INCORRECTO PARA REVERSION ARCHIVO PAGOS", "PARA REVERSION ARCHIVO PAGOS");
			}
			
			BigDecimal totalPlan = BigDecimal.ZERO;
			
			try {
				datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
				datos.agregarDato("montoPago", peticion.getValorMovimiento());
				transaccionesColector("00000000000000000000", datos);
				
				totalPlan = datos.obtenerBigDecimal("montoPago");
			} catch (EmptyResultDataAccessException e) {
				logger.error(NOM_COD_SERVICIO + "No existe registro de cuota para reversion. {}" + e.getMessage(), e);
				throw new ServicioException(20019, "No existe {} ", "REGISTRO DE CUOTA PARA REVERSION");
			}
			
			if (!UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), totalPlan)) {
				throw new ServicioException(20224, "VALOR DE LA TRANSACCION INCORRECTO CON TOTAL ARCHIVO CARGADO.", "CON TOTAL ARCHIVO CARGADO.");
			}
			
		}
		
		//11. Actualizar senial de reversion
		Object[] paramsAAATR = {
			Constantes.SI,
			datos.obtenerValor("fechaRelativa"),
			peticion.getCodOficinaTran(),
			peticion.getCodTerminal(),
			peticion.getNumReversa(),
			peticion.getCodTran(),
			relColectorDocumento.getInteger("codCausal"),
			relColectorDocumento.getInteger("codSubCausal"),
			peticion.getNumDocumentoTran(),
			peticion.getValorMovimiento(),
			Constantes.SI
		};
		logger.debug("Ejecutando sentencia UPDATE LINC SFBDB AAATR, parametros: {}" + Arrays.toString(paramsAAATR));
		ejecutarSentencia(query(UPDATE_LINC_SFBDB_AAATR), paramsAAATR);
		
		//11.1 Eliminar reversa perfiles 
		
		////////////
		////////////
//		invocando funcion utilizada para eliminar los registros correspondientes al glbDtime recibido como parametro de
//		las tablas perfiles de la tabla de transacciones AAATR. Ademas de insertar un registro en la tabla LINC.SFBDB_P_AAATR02
//		que donde se guardan las reversas.	
		
		datos.agregarDato("glbDtime", result);
		//codOficinaTran ya esta
		datos.agregarDato("codTerminalTran", datos.obtenerInteger("coTerminal"));
		//fechaRelativa ya esta
		datos.agregarDato("numTran", datos.obtenerInteger("nuReversa"));
		//numReversa ya esta
		actualizarPerfilesTransaccionAAATR(datos);
		///////////
		///////////
		
		//12. 
		
		if ((UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO))
				&&
			(
			UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_CUOTA_EXACTA) || 
			UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_MAYOR_CUOTA) || 
			UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GC_PAGO_DIFERENTE_CUOTA)
			)
			) {
			
			Object[] paramsICPGE = {
				new Integer(0),
				"I",
				peticion.getCodColector(),
				peticion.getCodTipoDocumento(),
				datos.obtenerValor("numCredito")
			};
			logger.debug("Ejecutando sentencia UPDATE LINC SFBDB ICPGE, parametros: {}" + Arrays.toString(paramsICPGE));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICPGE), paramsICPGE);
			
		}
		
		//13.
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO)) {
			
			////////////////////
			////////////////////
				
			Long glbDtimeICATR_SIN_PLANILLA = null;
			Object[] paramsSELECT_ICATR_SIN_PLANILLA = {					
					datos.obtenerValor("fechaSistemaAMD"),
					peticion.getCodOficinaTran(),
					peticion.getCodTerminal(),
					peticion.getCodCajero(),
					new Integer(0),
					StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
					peticion.getNumDocumentoTran(),
					peticion.getValorMovimiento(),
					Constantes.SI
				};
			Object[] paramsDELETE_ICATR_SIN_PLANILLA= new Object[1];
			try {				
				logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICATR, parametros: {}" + Arrays.toString(paramsSELECT_ICATR_SIN_PLANILLA));
				glbDtimeICATR_SIN_PLANILLA = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_ICATR_SIN_PLANILLA), Long.class, paramsSELECT_ICATR_SIN_PLANILLA);
				if(!UtileriaDeDatos.isNull(glbDtimeICATR_SIN_PLANILLA)) {
					paramsDELETE_ICATR_SIN_PLANILLA[0] = glbDtimeICATR_SIN_PLANILLA;
					logger.debug("Ejecutando sentencia DELETE_SFBDB_P_ICATR, parametros: " + Arrays.toString(paramsDELETE_ICATR_SIN_PLANILLA));
					ejecutarSentencia(query(DELETE_SFBDB_P_ICATR), paramsDELETE_ICATR_SIN_PLANILLA);
					logger.debug("Ejecutando sentencia DELETE_SFBDB_P_ICATR01, parametros: " + Arrays.toString(paramsDELETE_ICATR_SIN_PLANILLA));
					ejecutarSentencia(query(DELETE_SFBDB_P_ICATR01), paramsDELETE_ICATR_SIN_PLANILLA); 
				}
			}catch(EmptyResultDataAccessException e) {
				logger.error(NOM_COD_SERVICIO + "No existe registro de transaccion en ICATR.");
			}
			///////////////////
			//////////////////
			
			Object[] paramsICATR = {
				Constantes.SI,
				datos.obtenerValor("fechaSistemaAMD"),
				peticion.getCodOficinaTran(),
				peticion.getCodTerminal(),
				peticion.getCodCajero(),
				new Integer(0),
				StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
				peticion.getNumDocumentoTran(),
				peticion.getValorMovimiento(),
				Constantes.SI
			};
			logger.debug("Ejecutando sentencia UPDATE LINC SFBDB ICATR, parametros: {}" + Arrays.toString(paramsICATR));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICATR), paramsICATR);
		}
		
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.SI)) {
			Object[] paramsICPGE_1 = {
				new Integer(0),
				"I",
				peticion.getCodColector(),
				peticion.getCodTipoDocumento(),
				datos.obtenerValor("fechaSistemaAMD"),
				" "
			};
			logger.debug("Ejecutando sentencia UPDATE LINC SFBDB ICPGE, parametros: {}" + Arrays.toString(paramsICPGE_1));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICPGE_1), paramsICPGE_1);
			
			/////////////////////
			////////////////////
			Object[] paramsSELECT_ICATR_CON_PLANILLA = {
					datos.obtenerValor("fechaSistemaAMD"),
					peticion.getCodOficinaTran(),
					peticion.getCodTerminal(),
					peticion.getCodCajero(),
					new Integer(0),
					datos.obtenerValor("codClienteIEMCO"),
					peticion.getNumDocumentoTran(),
					Constantes.SI
				};
			Object[] paramsDELETE_ICATR_CON_PLANILLA= new Object[1];
			List<Map<String, Object>> registro = null;
			AdaptadorDeMapa adaptador = null; 
            Long glbDtime_P_ICATR_CON_PLANILLA = 0l;
            logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICATR CON_PLANILLA, parametros: {}" + Arrays.toString(paramsSELECT_ICATR_CON_PLANILLA));
			registro = jdbcTemplate.queryForList(query(SELECT_LINC_SFBDB_ICATR_CON_PLANILLA), paramsSELECT_ICATR_CON_PLANILLA);
			if(!UtileriaDeDatos.listIsEmptyOrNull(registro)) {
				for (Map<String, Object> mapa : registro) {
					adaptador = UtileriaDeDatos.adaptarMapa(mapa);
					glbDtime_P_ICATR_CON_PLANILLA = adaptador.getLong("glbDtimeICATR");
					paramsDELETE_ICATR_CON_PLANILLA[0] = glbDtime_P_ICATR_CON_PLANILLA;
					logger.debug("Ejecutando sentencia DELETE_SFBDB_P_ICATR, parametros: " + Arrays.toString(paramsDELETE_ICATR_CON_PLANILLA));
					ejecutarSentencia(query(DELETE_SFBDB_P_ICATR), paramsDELETE_ICATR_CON_PLANILLA);
					logger.debug("Ejecutando sentencia DELETE_SFBDB_P_ICATR01, parametros: " + Arrays.toString(paramsDELETE_ICATR_CON_PLANILLA));
					ejecutarSentencia(query(DELETE_SFBDB_P_ICATR01), paramsDELETE_ICATR_CON_PLANILLA);
				}				
			}
			////////////////////
			////////////////////
			
			Object[] paramsICATR_1 = {
				Constantes.SI,
				datos.obtenerValor("fechaSistemaAMD"),
				peticion.getCodOficinaTran(),
				peticion.getCodTerminal(),
				peticion.getCodCajero(),
				new Integer(0),
				datos.obtenerValor("codClienteIEMCO"),
				peticion.getNumDocumentoTran(),
				Constantes.SI
			};
			logger.debug("Ejecutando sentencia UPDATE LINC SFBDB ICATR CON_PLANILLA, parametros: {}" + Arrays.toString(paramsICATR_1));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICATR_1), paramsICATR_1);			
		}
			
		//14.
		Object periodoDeclaracion	= datos.obtenerValor("periodoDeclaracion");
		Integer fechaSistema		= datos.obtenerInteger("fechaSistema");
		String nomOficinaTran = datos.obtenerString("nomOficinaTran");
		datos.agregarDato("periodoPago", periodoDeclaracion);
		datos.agregarDato("fechaTransaccion", fechaSistema);
		datos.agregarDato("nomAgencia", nomOficinaTran);
		//nomAgencia ya esta en datos, puesto por seguridad
		//horaSistema ya esta en datos, puesto por seguridad
		//descTipoDocumento ya esta en datos, puesto por validar relación colector con documento de pago
	}
	
	private void transaccionesColector(String numCredito, DatosOperacion datos) throws TipoDatoException, ParseException, EmptyResultDataAccessException {
		
		ReversaPagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoColectorGenericoPeticion.class);
		Integer numDocumentoTran	= datos.obtenerInteger("numDocumentoTran");
		BigDecimal montoPago		= datos.obtenerBigDecimal("montoPago");
		Integer fechaSistemaAMD		= datos.obtenerInteger("fechaSistemaAMD");
		AdaptadorDeMapa relTransaccionSubCausal	= datos.obtenerObjeto("relTransaccionSubCausal", AdaptadorDeMapa.class);

		Object[] paramsICATR = {
			fechaSistemaAMD, relTransaccionSubCausal.getInteger("codOficinaCta"), peticion.getCodTerminal(), peticion.getCodCajero(),
			0, Constantes.SI, numCredito, numDocumentoTran, montoPago
		};
		logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICATR, parametros: {}" + Arrays.toString(paramsICATR));
		BigDecimal montoPagoRecuperado = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_ICATR), BigDecimal.class, paramsICATR);
		datos.agregarDato("montoPago", montoPagoRecuperado);
		
		
	}

	private void validarNumDocValMovimiento(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaPagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoColectorGenericoPeticion.class);
		AdaptadorDeMapa relColectorDocumento = datos.obtenerObjeto("relColectorDocumento", AdaptadorDeMapa.class);
		Integer codTipoCorrelativo = relColectorDocumento.getInteger("codTipoCorrelativo");
		//8.
		if (UtileriaDeDatos.isEquals(codTipoCorrelativo, new Integer(0)) &&
			UtileriaDeDatos.isEquals(peticion.getNumDocumentoTran(), new Integer(0))
			) {
			throw new ServicioException(20004, "DOCUMENTO INCORRECTO");
		}
		
	}

	private void obtenerValidarColectores(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaPagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoColectorGenericoPeticion.class);
		//7.1
		try {
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAGE, parametros: {}" + peticion.getCodColector() + "-" + "0");
			List<Map<String, Object>> queryForList = getJdbcTemplate().queryForList(query(SELECT_LINC_SFBDB_IEAGE),
																					peticion.getCodColector(), new Integer(0));
			
			if(UtileriaDeDatos.listIsEmptyOrNull(queryForList)) {
				throw new ServicioException(20019, "No existe {} ", "PARAMETROS DE COLECTOR");
			}
			
			Integer posicionAnterior		= 0;
			Integer longCampo				= 0;
			String numCredito				= "0";
			String codTipoIdentificacion	= null;//TODO Validar su uso
			String numIdentificacionCliente	= null;
			String tipoCliente				= null;//TODO Validar su uso
			String periodoDeclaracion		= "0";
			String valorMoraAux				= null;//TODO Validar su uso
			Integer senPlanilla				= Constantes.NO;
			String nomCliente				= "";
			Integer senCampo1				= 0;
			Integer senCampo3				= 0;
			Integer senCampo5				= 0;
			Integer senCampo6				= 0;
			Boolean fechaValida 			= Boolean.FALSE;
			
			for (Map<String, Object> map : queryForList) {
				AdaptadorDeMapa registro = UtileriaDeDatos.adaptarMapa(map);
				
				if (
					UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "NOCREDITO") && 
					UtileriaDeDatos.isEquals(registro.getInteger("senVisible"), Constantes.SI)
					) {
					longCampo = registro.getInteger("longitudCampo");
					numCredito = StringUtils.leftPad(peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo), 20, '0');
					
					if (UtileriaDeDatos.isEquals(registro.getInteger("senObligatorio"), Constantes.SI)) {
						senCampo1 = Constantes.SI;
					}
					posicionAnterior += longCampo;
					
				}
				
				if (
						UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "ICOTIPIDE") && 
						UtileriaDeDatos.isEquals(registro.getInteger("senVisible"), Constantes.SI)
						) {
						longCampo = registro.getInteger("longitudCampo");
						codTipoIdentificacion = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						posicionAnterior += longCampo;
						
					}
				
				if (
						UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "INUIDECLI") && 
						UtileriaDeDatos.isEquals(registro.getInteger("senVisible"), Constantes.SI)
						) {
						longCampo = registro.getInteger("longitudCampo");
						numIdentificacionCliente = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						
						if (UtileriaDeDatos.isEquals(registro.getInteger("senObligatorio"), Constantes.SI)) {
							senCampo3 = Constantes.SI;
						}
						posicionAnterior += longCampo;
						
					}
				
				if (
						UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "ICOTIPCLI") && 
						UtileriaDeDatos.isEquals(registro.getInteger("senVisible"), Constantes.SI)
						) {
						longCampo = registro.getInteger("longitudCampo");
						tipoCliente = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						posicionAnterior += longCampo;
						
					}
				
				if (
						UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "IPE-DECLAR") && 
						UtileriaDeDatos.isEquals(registro.getInteger("senVisible"), Constantes.SI)
						) {
						longCampo = registro.getInteger("longitudCampo");
						periodoDeclaracion = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						
						if (UtileriaDeDatos.isEquals(registro.getInteger("senObligatorio"), Constantes.SI)) {
							senCampo5 = Constantes.SI;
						}
						posicionAnterior += longCampo;
						
					}
				
				if (
					UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "IVALMORA") && 
					UtileriaDeDatos.isEquals(registro.getInteger("senVisible"), Constantes.SI)
					) {
						longCampo = registro.getInteger("longitudCampo");
						valorMoraAux = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						posicionAnterior += longCampo;
						
					}
				
				if (
						UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "CARGA") && 
						UtileriaDeDatos.isEquals(registro.getInteger("senArchivo"), Constantes.SI)
						) {
						senPlanilla = registro.getInteger("senArchivo");
						posicionAnterior += longCampo;
					}
				
				if (
						UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "INO-CLIEN") && 
						UtileriaDeDatos.isEquals(registro.getInteger("senVisible"), Constantes.SI)
						) {
						longCampo = registro.getInteger("longitudCampo");
						nomCliente = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						
						if (UtileriaDeDatos.isEquals(registro.getInteger("senObligatorio"), Constantes.SI)) {
							senCampo6 = Constantes.SI;
						}
						posicionAnterior += longCampo;
						
					}
				
				if (UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "MMAAXX")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(periodoDeclaracion, "MMAA");
				}
				
				if (UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "DDMMAA")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(periodoDeclaracion, "DDMMAA");
				}
				
				if (UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "AAMMDD")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(periodoDeclaracion, "AAMMDD");
				}
				
				if (UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "MMAAAA")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(periodoDeclaracion, "MMAAAA");
				}
				
				if (UtileriaDeDatos.isEquals(registro.getString("nombreCampo"), "AAAAMM")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(periodoDeclaracion, "AAAAMM");
				}
				
			}
			
			//7.2
			
			if (
				UtileriaDeDatos.isEquals(senCampo1, Constantes.SI) && 
				UtileriaDeDatos.isEquals(StringUtils.leftPad(numCredito, 20, '0'), "00000000000000000000")
				) {
				throw new ServicioException(20589, "Numero incorrecto");
			}
			
			if (
				UtileriaDeDatos.isEquals(senCampo3, Constantes.SI) && 
				UtileriaDeDatos.isEquals(StringUtils.leftPad(numIdentificacionCliente, 14, '0'), "00000000000000")
					) {
					throw new ServicioException(20589, "Numero incorrecto");
				}
			
			if (
				!fechaValida && 
				UtileriaDeDatos.isEquals(senCampo5, Constantes.SI)
						) {
						throw new ServicioException(20003, "Fecha incorrecta {}", " PERIODO NO VALIDO");
					}
			
			if (
				UtileriaDeDatos.isEquals(senCampo6, Constantes.SI) && 
				UtileriaDeDatos.isEquals(nomCliente.length(), new Integer(0))
						) {
						throw new ServicioException(20569, "Parametro requerido - ingrese nombre del cliente", "INGRESE NOMBRE DEL CLIENTE");
					}
			
			datos.agregarDato("posicionAnterior", posicionAnterior);
			datos.agregarDato("longCampo", longCampo);
			datos.agregarDato("numCredito", numCredito);
			datos.agregarDato("codTipoIdentificacion", codTipoIdentificacion);
			datos.agregarDato("numIdentificacionCliente", numIdentificacionCliente);
			datos.agregarDato("tipoCliente", tipoCliente);
			datos.agregarDato("periodoDeclaracion", periodoDeclaracion);
			datos.agregarDato("valorMoraAux", valorMoraAux);
			datos.agregarDato("senPlanilla", senPlanilla);
			datos.agregarDato("nomCliente", nomCliente);
			datos.agregarDato("senCampo1", senCampo1);
			datos.agregarDato("senCampo3", senCampo3);
			datos.agregarDato("senCampo5", senCampo5);
			datos.agregarDato("senCampo6", senCampo6);
			datos.agregarDato("fechaValida", fechaValida);
			
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "NO EXISTE PARAMETROS DE COLECTOR {}", erdae);
			throw new ServicioException(20019, "No existe {} ", "PARAMETROS DE COLECTOR");
		}
		
	}

	private void validaciones(DatosOperacion datos) throws TipoDatoException, ServicioException, ParseException {
		
		ReversaPagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoColectorGenericoPeticion.class);
		
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		Date fechaSistemaD = UtileriaDeDatos.fecha6ToDate(fechaSistema);
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistemaD);
		
		if (UtileriaDeDatos.isEquals(peticion.getCodTipoDocumento(), new Integer(0))) {
			peticion.setCodTipoDocumento(new Integer(1));
		}
		
		String codClienteIEMCO = "";
		try {
			logger.debug("Ejecutando sentencia SELECT SFBDB IEMCO, parametro: {}", peticion.getCodColector());
			codClienteIEMCO = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_IEMCO), String.class, peticion.getCodColector());
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "NO EXISTE COLECTOR DEFINIDO EN LA IEMCO {}", erdae);
			throw new ServicioException(20019, "No existe {} ", "COLECTOR DEFINIDO EN LA IEMCO");
		}

		AdaptadorDeMapa relColectorDocumento = null;
		try {
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICRDO, parametros: {}" + peticion.getCodColector() + "-" + peticion.getCodTipoDocumento());
			Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_ICRDO), 
												peticion.getCodColector(), peticion.getCodTipoDocumento());
			relColectorDocumento = UtileriaDeDatos.adaptarMapa(queryForMap);
			
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "NO EXISTE RELACIÓN TIPO DE DOCUMENTO CON COLECTOR {}", erdae);
			throw new ServicioException(20019, "No existe {} ", "RELACION TIPO DE DOCUMENTO CON COLECTOR");
		}

		String codPantalla = Constantes.ISPEC_AJ499;
		Integer codCausal = relColectorDocumento.getInteger("codCausal");
		Integer senValidacionRegistro = relColectorDocumento.getInteger("senValidacionRegistro");
		String descTipoDocumento = relColectorDocumento.getString("descTipoDocumento");
		Integer senSubCausal = null;
		try {
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAMTM, parametros: {}" + codPantalla + "-" + codCausal);
			senSubCausal = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_AAMTM), Integer.class, codPantalla, codCausal);
			
			if (UtileriaDeDatos.isNull(senSubCausal)) {
				logger.error(NOM_COD_SERVICIO + "NO EXISTE CODIGO DE CAUSAL/TRANSACCION");
				throw new ServicioException(20019, "No existe {} ", "CODIGO DE CAUSAL/TRANSACCION");
			}else if(!UtileriaDeDatos.isEquals(senSubCausal, Constantes.SI)){
				logger.error(NOM_COD_SERVICIO + "SEÑAL INCORRECTA DE USO SUBCAUSAL EN AAMTM");
				throw new ServicioException(20005, "SEÑAL INCORRECTA DE USO SUBCAUSAL EN AAMTM");
			}
			
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "NO EXISTE CODIGO DE CAUSAL/TRANSACCION {}", erdae);
			throw new ServicioException(20019, "No existe {} ", "CODIGO DE CAUSAL/TRANSACCION");
		}
		
		Integer codSubCausal = relColectorDocumento.getInteger("codSubCausal");
		AdaptadorDeMapa relTransaccionSubCausal = null;
		try {
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAMSM, parametros: {}" + codPantalla + "-" + codCausal + "-" + codSubCausal);
			Map<String, Object> queryForMap1 = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_AAMSM), codPantalla, codCausal, codSubCausal);
			relTransaccionSubCausal = UtileriaDeDatos.adaptarMapa(queryForMap1);
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "NO EXISTE CODIGO DE SUBCAUSAL/TRANSACCION {}", erdae);
			throw new ServicioException(20019, "No existe {} ", "CODIGO DE SUBCAUSAL/TRANSACCION");
		}

		String cuentaTransaccion = relTransaccionSubCausal.getString("cuentaStr");
		Integer codProductoCta = relTransaccionSubCausal.getInteger("codProductoCta");
		String conceptoProducto = codProductoCta.toString().substring(0,1);
		
		Cliente cliente = null;
		if (UtileriaDeDatos.isEquals(new Integer(conceptoProducto), Constantes.CONCEPTO_CC)) {
			CuentaCorriente pcc = recuperarDatosCuentaCorriente(cuentaTransaccion);
			cliente = recuperarDatosCliente(pcc.getCodCliente());
		}
		
		if (UtileriaDeDatos.isEquals(new Integer(conceptoProducto), Constantes.CONCEPTO_AH)) {
			CuentaAhorro pca = recuperarDatosCuentaAhorro(cuentaTransaccion);
			cliente = recuperarDatosCliente(pca.getCodCliente());
		}
		
		datos.agregarDato("codCausal", codCausal);
		datos.agregarDato("codSubCausal", codSubCausal);
		datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
		datos.agregarDato("codClienteIEMCO", codClienteIEMCO);
		datos.agregarDato("relColectorDocumento", relColectorDocumento);
		datos.agregarDato("senSubCausal", senSubCausal);
		datos.agregarDato("relTransaccionSubCausal", relTransaccionSubCausal);
		datos.agregarDato("cliente", cliente);
		datos.agregarDato("senValidacionRegistro", senValidacionRegistro);
		datos.agregarDato("descTipoDocumento", descTipoDocumento);
	}

	/**
	 * M&eacutetodo para realizar validaciones iniciales sobre los par&aacutemetros recibidos.
	 * @param peticion
	 * @throws ServicioException
	 */
	private void validadacionInicial(ReversaPagoColectorGenericoPeticion peticion) throws ServicioException {
		logger.debug(NOM_COD_SERVICIO + "Iniciando validacion de parametros");
		logger.debug(NOM_COD_SERVICIO + "Peticion recibida: {}", peticion);
		
		UtileriaDeParametros.validarParametro(peticion.getCodColector(), "codColector", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTipoDocumento(), "codTipoDocumento", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumReversa(), "numReversa", TipoValidacion.ENTERO_MAYOR_CERO);
		
	}
	
}
