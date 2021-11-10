package sv.gob.bfa.pagaduriagenerico.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import sv.gob.bfa.pagaduriagenerico.model.ReversaPagaduriaGenericoPeticion;
import sv.gob.bfa.pagaduriagenerico.model.ReversaPagaduriaGenericoRespuesta;

public class ReversaPagaduriaGenericoServicio extends Servicio{

	private static final String NOM_COD_SERVICIO = "Reversa Pagaduria Generico AJ599: ";
	
	private static final String SELECT_LINC_SFBDB_IEMCO = 
			"SELECT SCO_IDENT AS codClienteIEMCO" + 
			"	FROM LINC.SFBDB_IEMCO@DBLINK@" + 
			"	WHERE ICO_COLECT = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_ICRDO = 
			"SELECT ACO_CAUSA AS codCausal," + 
			"		ACOSUBCAU AS codSubCausal," + 
			"		ISEVALREG AS senValidacionRegistro," + 
			"		ACOTIPCOR AS codTipoCorrelativo," + 
			"		IDEDOCUM AS descTipoDocumento" + 
			"	FROM LINC.SFBDB_ICRDO@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND ICOTIPDOC  = ?"
			;
	
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
			"   	AND INU_CAMPO > ?"
			;
	
	private static final String SELECT_LINC_SFBDB_AAATR = 
			"SELECT 1" + 
			"	FROM LINC.SFBDB_AAATR@DBLINK@" + 
			"   WHERE	TFETRAREL = ?" + 
			"		AND DCO_OFICI = ?" + 
			"		AND DCO_TERMI = ?" + 
			"		AND TNU_TRANS = ?" + 
			"		AND DCO_TRANS = ?" + 
			"		AND ACO_CAUSA = ?" + 
			"		AND ACOSUBCAU = ?" + 
			"		AND TNUDOCTRA = ?" + 
			"		AND TVA_MOVIM = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_ICPGE = 
			"SELECT INO_CLIEN AS nomCliente,	" + 
			"GLB_DTIME AS dTime,				" +		
			"INUREGCLI AS numRegistroCliente,	" + 
			"ICO_ESTAD AS codEstado,			" + 
			"IFE_PAGO AS fechaPago,				" + 
			"IVA_PAGO AS valorPago,				" + 
			"ICOTIPCLI AS tipoCliente,			" + 
			"IFE_CARGA AS fechaCarga,			" + 
			"IHO_CARGA AS horaCarga,			" + 
			"IFE_VENCI AS fechaVencimiento,		" + 
			"SUBSTR(INUIDECLI, 1,8) AS convenio FROM " + 
			"( " + 
			"SELECT 	*	" + 
			"FROM LINC.SFBDB_ICPGE@XCAJA 	" + 
			"WHERE	ICO_ESTAD = ?	" + 
			"AND ICO_COLECT = ? 	" + 
			"AND ICOTIPDOC = ? 		" + 
			"AND INUREGCLI = ? 		" + 
			"AND INUIDECLI = ?		" + 
			"AND IFE_PAGO  = ?		" + 
			"AND GLB_DTIME = ?      " + 
			") " + 
			"WHERE ROWNUM <= 1 "
			;
	
	private static final String SELECT_LINC_SFBDB_ICPGE_1 = 
			"SELECT INO_CLIEN AS nomCliente," + 
			"GLB_DTIME AS dTime,				" +	
			"		SUBSTR(INUIDECLI, 1,8) AS convenio," + 
			"		ICO_ESTAD AS codEstado," + 
			"		IFE_PAGO AS fechaPago" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_ESTAD = ?" + 
			" 		AND ICO_COLECT = ?" + 
			" 		AND ICOTIPDOC = ?" + 
			" 		AND INUREGCLI = ?" + 
			" 		AND INUIDECLI = ?" + 
			" 		AND IFE_PAGO  = ?" + 
			"		AND GLB_DTIME = ?" + 
			"		AND ROWNUM <= 1"
			;
	
	private static final String SELECT_LINC_SFBDB_ICPGE_2 = 
			"SELECT INO_CLIEN AS nomCliente," + 
			"GLB_DTIME AS dTime,				" +	
			"		INUREGCLI AS numRegistroCliente," + 
			"		ICO_ESTAD AS codEstado," + 
			"		IFE_PAGO AS fechaPago," + 
			"		IVA_PAGO AS valorPago," + 
			"		IFE_VENCI AS fechaVencimiento," + 
			"		SUBSTR(INUIDECLI, 1,8) AS convenio" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_ESTAD = ?" + 
			" 		AND ICO_COLECT = ?" + 
			" 		AND ICOTIPDOC = ?" + 
			"		AND INUREGCLI = ?" + 
			"		AND INU_DOCUM = ?" + 
			"		AND IFE_PAGO  = ?" + 
			"		AND GLB_DTIME = ?" +
			"		AND ROWNUM <= 1"
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
	
	private static final String SELECT_LINC_SFBDB_ICPGE_TOTAL = 
			"SELECT SUM(IVA_PAGO) AS totalPlan" + 
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
			"	SET IFE_PAGO = ?" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"      	AND ICOTIPDOC = ?" + 
			"      	AND INUREGCLI = ?" + 
			"		AND INUIDECLI = ?" + 
			"		AND GLB_DTIME = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_ICPGE2 = 
			"UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET IFE_PAGO = ?," + 
			"	ICO_ESTAD = ?" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"      	AND ICOTIPDOC = ?" + 
			"      	AND INUREGCLI = ?" + 
			"		AND INUIDECLI = ?" +
			"		AND IFE_PAGO = ?" + 
			"		AND GLB_DTIME = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_ICPGE_3 = 
			"UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET INU_DOCUM = ?," + 
			"		ICO_ESTAD = ?," + 
			"		IFE_PAGO  = ?" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"    	AND ICOTIPDOC = ?" + 
			"    	AND INUREGCLI = ?" + 
			"		AND GLB_DTIME = ?"		
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
	
	private static final String UPDATE_LINC_SFBDB_ICPGE_4 = 
			"UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET IFE_PAGO = ?," + 
			"		ICO_ESTAD = ?" + 
			"	WHERE	ICO_COLECT = ?" + 
			"   	AND ICOTIPDOC = ?" + 
			"   	AND IFE_PAGO = ?" + 
			"   	AND INU_DOCUM = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_ICATR_5 = 
			"UPDATE LINC.SFBDB_ICATR@DBLINK@" + 
			"	SET TSE_REVER = ?" + 
			"	WHERE	IFE_TRANS = ?" + 
			" 		AND DCO_OFICI = ?" + 
			" 		AND DCO_TERMI = ?" + 
			" 		AND DCO_USUAR = ?" + 
			" 		AND INU_REGIS > ?" + 
			" 		AND INUREGCLI = ?" + 
			" 		AND TNUDOCTRA = ?" + 
			" 		AND TSE_REVER != ?"
			;
	
	Logger logger = LoggerFactory.getLogger(ReversaPagaduriaGenericoServicio.class);
	
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		
		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");

		logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();

		logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> ReversaPagaduriaGenericoPeticion");
		ReversaPagaduriaGenericoPeticion peticion = (ReversaPagaduriaGenericoPeticion) objetoDom;
		
		try {
		
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			//1.
			validacionInicial(peticion);
			datos.agregarDato("peticion",peticion);
			
			//2.
			seguridadTerminales(datos);
			
			//3. 4. 5. y 6. Validaciones
			validacionesBase(datos);
			
			//7.
			obtenerValidarColectores(datos);
			
			//8. y 9.
			validarNumDocValMovimiento(datos);
			
			//10. 11. 12. 13 y 14.
			validarParametrosRegistro(datos);
			
			//Preparacion de objeto respuesta del servicio.
			ReversaPagaduriaGenericoRespuesta respuesta = new ReversaPagaduriaGenericoRespuesta();
			datos.llenarObjeto(respuesta);
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");

			return respuesta;
		} catch (ServicioException se) {
			throw manejarMensajeExcepcionServicio(se);
		} catch (TipoDatoException | ParseException tde) {
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + tde.getMessage()));
		}
		
	}
	
	private void validarParametrosRegistro(DatosOperacion datos) throws TipoDatoException, ServicioException, ParseException {
		//10.
		ReversaPagaduriaGenericoPeticion peticion		= datos.obtenerObjeto("peticion", ReversaPagaduriaGenericoPeticion.class);
		AdaptadorDeMapa adaptadorICRDO					= datos.obtenerObjeto("adaptadorICRDO", AdaptadorDeMapa.class);
		Integer senValidacionRegistro					= adaptadorICRDO.getInteger("senValidacionRegistro");
		
		if (UtileriaDeDatos.isGreater(senValidacionRegistro, Constantes.GP_PAGO_DIFERENTE_CUOTA) || 
			UtileriaDeDatos.isEquals(senValidacionRegistro, new Integer(0))) {
			throw new ServicioException(20018, "Valor incorrecto, parametro de validacion de registro", "PARAMETRO DE VALIDACION DE REGISTRO");
		}
		
		Integer fechaRelativa = datos.obtenerInteger("fechaRelativa");
		try {
			
			Object[] paramsAAATR = {
				fechaRelativa, peticion.getCodOficinaTran(), peticion.getCodTerminal(), peticion.getNumReversa(),
				peticion.getCodTran(), adaptadorICRDO.getInteger("codCausal"), adaptadorICRDO.getInteger("codSubCausal"), 
				peticion.getNumDocumentoTran(), peticion.getValorMovimiento()
			};
			
			Integer result = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_AAATR), Integer.class, paramsAAATR);
			
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "No existe la operacion realizada.");
			throw new ServicioException(20212, "No existe la operacion realizada.");
		}
		
		Integer numRegistroCliente	= new Integer(0);
		Integer numRegistroCLienteIEMCO = new Integer(0);
		Integer senPlanilla			= datos.obtenerInteger("senPlanilla");
		AdaptadorDeMapa infoCliente = null;
		String codPlanilla			= "";
		String programaFISDL		= "";
		
		Boolean senSQL1 = Boolean.FALSE;
		Boolean senSQL2 = Boolean.FALSE;
		
		Integer codColector = peticion.getCodColector();
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO)) {
			
			if (UtileriaDeDatos.isEquals(codColector, Constantes.GP_COLECTOR_FISDL) || 
				UtileriaDeDatos.isEquals(codColector, Constantes.GP_COLECTOR_FISDL_INDEMNIZATORIO)
				) {
				
				try {
					
					Object[] paramsICPGE = {
						Constantes.GP_PAGO,
						peticion.getCodColector(),
						peticion.getCodTipoDocumento(),
						StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
						StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0'),
						datos.obtenerValor("fechaSistemaAMD"),
						peticion.getGlbDtime()
					};
					logger.debug("Ejecutando sentencia SELECT_LINC_SFBDB_ICPGE, parametros: {}", Arrays.toString(paramsICPGE));
					Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_ICPGE), paramsICPGE);
					infoCliente	= UtileriaDeDatos.adaptarMapa(queryForMap);
					
					senSQL1 = Boolean.TRUE;
					
				} catch (EmptyResultDataAccessException erdae) {
					
					try {
						Object[] paramsICPGE = {
								Constantes.GP_PAGO,
								peticion.getCodColector(),
								peticion.getCodTipoDocumento(),
								datos.obtenerValor("numCredito"),
								StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0'),
								datos.obtenerValor("fechaSistemaAMD"),
								peticion.getGlbDtime()
							};
						logger.debug("Ejecutando sentencia SELECT_LINC_SFBDB_ICPGE, parametros: {}", Arrays.toString(paramsICPGE));
						Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_ICPGE_1), paramsICPGE);
						infoCliente = UtileriaDeDatos.adaptarMapa(queryForMap);
						senSQL2 = Boolean.TRUE;
					} catch (EmptyResultDataAccessException e) {
						logger.error(NOM_COD_SERVICIO + "NO EXISTE REGISTRO DE CUOTA BASE-COLECTOR");
						throw new ServicioException(20019, "NO EXISTE REGISTRO DE CUOTA BASE-COLECTOR", "REGISTRO DE CUOTA BASE-COLECTOR -" + 
													datos.obtenerString("numCredito") + "-" + datos.obtenerString("numIdentificacionCliente"));
					}
				}
				
			} else {

				try {
					Object[] paramsICPGE = {
							Constantes.GP_PAGO,
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
							peticion.getNumDocumentoTran(),
							datos.obtenerInteger("fechaSistemaAMD"),
							peticion.getGlbDtime()
						};
					
					logger.debug("EJECUTANDO SENTENCIA SELEC LINC SFBDB ICPGE 2, parametros: {}", Arrays.toString(paramsICPGE));
					Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_ICPGE_2), paramsICPGE);
					infoCliente = UtileriaDeDatos.adaptarMapa(queryForMap);
					
				} catch (EmptyResultDataAccessException erdae) {
					throw new ServicioException(20019, "NO EXISTE REGISTRO SUBSIDIO PARA REVERSION", "REGISTRO SUBSIDIO PARA REVERSION");
				}
				
			}
			
			if (
					(
						UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GP_PAGO_CUOTA_EXACTA) 
																	|| 
						UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GP_PAGO_MAYOR_CUOTA) 
																	|| 
						UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GP_PAGO_DIFERENTE_CUOTA)
					)
																	&&
					(
						!UtileriaDeDatos.isEquals(infoCliente.getString("codEstado"), "P") 
																	|| 
						UtileriaDeDatos.isEquals(infoCliente.getInteger("fechaPago"), new Integer(0))
					)
				) {
				throw new ServicioException(20016, "Estado Incorrecto {} ", " DEL REGISTRO PARA REVERSION") ;
			}
			datos.agregarDato("dTime", infoCliente.getLong("dTime"));
			datos.agregarDato("nomCliente", infoCliente.getString("nomCliente"));
			datos.agregarDato("convenio", infoCliente.getString("convenio"));//Requerido como parametro de salida del servicio
			
			try {
				//numCredito ya esta
				datos.agregarDato("numRef", " ");
				datos.agregarDato("numDocumentoTranAux", peticion.getNumDocumentoTran());
				datos.agregarDato("montoPago", peticion.getValorMovimiento());
				transaccionesColector(datos);
			} catch (EmptyResultDataAccessException e) {
				logger.error(NOM_COD_SERVICIO + "No existe registro de cuota para reversion. {}" + e.getMessage(), e);
				throw new ServicioException(20019, "NO EXISTE REGISTRO SUBSIDIO PARA REVERSION", "REGISTRO SUBSIDIO PARA REVERSION");
			}
			
			codPlanilla = peticion.getNumDocumentoTran().toString();
			programaFISDL 		= adaptadorICRDO.getString("descTipoDocumento");
			datos.agregarDato("codPlanilla", codPlanilla);//Parametro de salida del servicio
			datos.agregarDato("descTipoDocumento", programaFISDL);//Parametro de salida del servicio
			datos.agregarDato("programaFISDL", programaFISDL);//TODO validar cual de los dos nombre se queda para este paremtro
		}
		
		BigDecimal totalPlan = null;
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.SI)) {
					
			try {

				Object[] paramsICPGE = {
						peticion.getCodColector(),
						peticion.getCodTipoDocumento(),
						datos.obtenerInteger("fechaSistemaAMD"),
						" "
				};

				totalPlan = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_ICPGE_TOTAL), BigDecimal.class, 
						paramsICPGE);

				datos.agregarDato("totalPlan", totalPlan);

			} catch (EmptyResultDataAccessException erdae) {
				logger.error(NOM_COD_SERVICIO + "NO EXISTE ARCHIVO PAGADO PARA REVERSION");
				throw new ServicioException(20019, "NO EXISTE ARCHIVO PAGADO PARA REVERSION", "ARCHIVO PAGADO PARA REVERSION");
			}
					
			if (!UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), totalPlan)) {
				throw new ServicioException(20224, "VALOR DE LA TRANSACCION INCORRECTO PARA REVERSION ARCHIVO PAGOS", "PARA REVERSION ARCHIVO PAGOS");
			}
			
			totalPlan = BigDecimal.ZERO;
			
			try {
				datos.agregarDato("numRef", " ");
				datos.agregarDato("numDocumentoTranAux", peticion.getNumDocumentoTran());
				datos.agregarDato("montoPago", peticion.getValorMovimiento());
				transaccionesColector(datos);
				totalPlan = datos.obtenerBigDecimal("montoPago");
			} catch (EmptyResultDataAccessException e) {
				logger.error(NOM_COD_SERVICIO + "No existe registro subsidio para reversion. {}" + e.getMessage(), e);
				throw new ServicioException(20019, "NO EXISTE REGISTRO SUBSIDIO PARA REVERSION", "REGISTRO SUBSIDIO PARA REVERSION");
			}
			
			if (!UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), totalPlan)) {
				throw new ServicioException(20224, "VALOR DE LA TRANSACCION INCORRECTO CON TOTAL ARCHIVO CARGADO.", "CON TOTAL ARCHIVO CARGADO.");
			}
			
			numRegistroCLienteIEMCO = datos.obtenerInteger("numRegistroCLienteIEMCO");
			
		}//FIN PLANILLA = SI
		
		//11. Actualizar senial de reversion
		Object[] paramsAAATR = {
			Constantes.SI,
			datos.obtenerValor("fechaRelativa"),
			peticion.getCodOficinaTran(),
			peticion.getCodTerminal(),
			peticion.getNumReversa(),
			peticion.getCodTran(),
			adaptadorICRDO.getInteger("codCausal"),
			adaptadorICRDO.getInteger("codSubCausal"),
			peticion.getNumDocumentoTran(),
			peticion.getValorMovimiento(),
			Constantes.SI
		};
		logger.debug("Ejecutando sentencia UPDATE_LINC_SFBDB_AAATR, parametros: {}", Arrays.toString(paramsAAATR));
		ejecutarSentencia(query(UPDATE_LINC_SFBDB_AAATR), paramsAAATR);
		
		//12. 
		
		if ((UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO))
				&&
			(
			UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GP_PAGO_CUOTA_EXACTA) || 
			UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GP_PAGO_MAYOR_CUOTA) || 
			UtileriaDeDatos.isEquals(senValidacionRegistro, Constantes.GP_PAGO_DIFERENTE_CUOTA)
			)
			) {
			
			if (senSQL1) {
				Object[] paramsICPGE = {
						new Integer(0),
						Constantes.GP_PAGO,
						peticion.getCodColector(),
						peticion.getCodTipoDocumento(),
						datos.obtenerString("numCredito"), 
						StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"),14,'0'),
						peticion.getGlbDtime()
					};
				logger.debug("Ejecutando sentencia UPDATE_LINC_SFBDB_ICPGE, parametros: {}", Arrays.toString(paramsICPGE));
				getJdbcTemplate().update(query(UPDATE_LINC_SFBDB_ICPGE), paramsICPGE);
			}
			
			if (senSQL2) {
				Object[] paramsICPGE2 = {
					new Integer(0),
					Constantes.GP_INGRESO,
					Constantes.GP_PAGO,
					peticion.getCodColector(),
					peticion.getCodTipoDocumento(),
					StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
					StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"),14,'0'),
					datos.obtenerInteger("fechaSistemaAMD"),
					peticion.getGlbDtime()
				};
				logger.debug("Ejecutando sentencia UPDATE_LINC_SFBDB_ICPG2, parametros: {}", Arrays.toString(paramsICPGE2));
				ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICPGE2), paramsICPGE2);
			}
			
		}
		
		if (
				(UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO))
											&&	
				(
						!UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL) 
																|| 
				        !UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL_INDEMNIZATORIO)
				)
			) {
			

			Object[] paramsICPGE3 = {
					" ",
					Constantes.GP_INGRESO,
					new Integer(0),
					Constantes.GP_PAGO,
					peticion.getCodColector(),
					peticion.getCodTipoDocumento(),
					datos.obtenerValor("numCredito"),
					peticion.getGlbDtime()
			};
			logger.debug("Ejecutando sentencia UPDATE_LINC_SFBDB_ICPG3, parametros: {}", Arrays.toString(paramsICPGE3));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICPGE_3), paramsICPGE3);
		}
		
		//13.
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO)) {
			
			Object[] paramsICATR = {
				Constantes.SI,
				datos.obtenerValor("fechaSistemaAMD"),
				peticion.getCodOficinaTran(),
				peticion.getCodTerminal(),
				peticion.getCodCajero(),
				new Integer(0),
				datos.obtenerValor("numCredito"),
				peticion.getNumDocumentoTran(),
				peticion.getValorMovimiento(),
				Constantes.SI
			};
			
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICATR), paramsICATR);
		}
		
		if (UtileriaDeDatos.isEquals(senPlanilla, Constantes.SI)) {
			Object[] paramsICPGE4 = {
				new Integer(0),
				Constantes.GP_INGRESO,
				peticion.getCodColector(),
				peticion.getCodTipoDocumento(),
				datos.obtenerValor("fechaSistemaAMD"),
				" "
			};
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICPGE_4), paramsICPGE4);
			
			Object[] paramsICATR5 = {
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
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_ICATR_5), paramsICATR5);
			
		}

		//14.
		String impFISDL = "";
		if (!UtileriaDeDatos.isNull(datos.obtenerValor("nomCliente"))) {
			impFISDL = "(Nomb:) " + datos.obtenerString("nomCliente") + impFISDL;
		}
		Object periodoDeclaracion	= datos.obtenerValor("periodoDeclaracion");
		Integer fechaSistema		= datos.obtenerInteger("fechaSistema");
		String nomOficinaTran 		= datos.obtenerString("nomOficinaTran");
		datos.agregarDato("periodoPago", periodoDeclaracion);
		datos.agregarDato("fechaTransaccion", fechaSistema);
		datos.agregarDato("nomAgencia", nomOficinaTran);
		
		Cliente cliente = datos.obtenerObjeto("cliente", Cliente.class);
		datos.agregarDato("tipoDocumento", "");
		datos.agregarDato("dui", "");
		if (!UtileriaDeDatos.isNull(cliente)) {
			if (UtileriaDeDatos.isBlank(datos.obtenerString("nomCliente"))) {
				datos.agregarDato("nomCliente", cliente.getNombreCompletoCliente());
			}
			datos.agregarDato("tipoDocumento", cliente.getTipDocumentoCliente());
			datos.agregarDato("dui", cliente.getDuiCliente());
		}
		
		datos.agregarDato("impFISDL", impFISDL);
		
	}
	
	private void transaccionesColector(DatosOperacion datos) throws TipoDatoException, ParseException, EmptyResultDataAccessException {
		
		ReversaPagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagaduriaGenericoPeticion.class);
		String numRef			= datos.obtenerString("numRef");
		Integer fechaSistemaAMD		= datos.obtenerInteger("fechaSistemaAMD");

		Object[] paramsICATR = {
			fechaSistemaAMD, peticion.getCodOficinaTran(), peticion.getCodTerminal(), peticion.getCodCajero(),
			0, Constantes.SI, StringUtils.leftPad(numRef, 20, '0'), peticion.getNumDocumentoTran(), peticion.getValorMovimiento(),
		};
		
		BigDecimal recuperado = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_ICATR), BigDecimal.class, paramsICATR);
		
		datos.agregarDato("montoPago", recuperado);
		
	}

	private void validarNumDocValMovimiento(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaPagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagaduriaGenericoPeticion.class);
		AdaptadorDeMapa adaptadorICRDO = datos.obtenerObjeto("adaptadorICRDO", AdaptadorDeMapa.class);
		Integer codTipoCorrelativo = adaptadorICRDO.getInteger("codTipoCorrelativo");
		//8.
		if (UtileriaDeDatos.isEquals(codTipoCorrelativo, new Integer(0)) &&
			UtileriaDeDatos.isEquals(peticion.getNumDocumentoTran(), new Integer(0))
			) {
			throw new ServicioException(20004, "Documento ya existe registrado para la cuenta");
		}
		//9.
//		if (!UtileriaDeDatos.isEquals(peticion.getValorEfectivo(), peticion.getValorMovimiento())) {
//			throw new ServicioException(20286, "Valor del movimiento no esta cuadrado.");
//		}
		
	}
	
	private void obtenerValidarColectores(DatosOperacion datos) throws TipoDatoException, ServicioException {
		ReversaPagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagaduriaGenericoPeticion.class);
		//7.1
		try {
			List<Map<String, Object>> queryForList = getJdbcTemplate().queryForList(query(SELECT_LINC_SFBDB_IEAGE),
																					peticion.getCodColector(), new Integer(0));
			
			Integer posicionAnterior		= 0;
			Integer longCampo				= 0;
			String numCredito				= null;
			String codTipoIdentificacion	= null;//TODO Validar su uso
			String numIdentificacionCliente	= null;
			String tipoCliente				= null;//TODO Validar su uso
			String periodoDeclaracion		= "";
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
						throw new ServicioException(20003, "Fecha incorrecta {}", " NUMERO INCORRECTO");
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
			throw new ServicioException(20019, "NO EXISTE PARAMETROS DE COLECTOR", "PARAMETROS DE COLECTOR");
		}
	}

	private void validacionesBase(DatosOperacion datos) throws TipoDatoException, ServicioException {
		ReversaPagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagaduriaGenericoPeticion.class);
		
		//3.
		if (UtileriaDeDatos.isEquals(peticion.getCodTipoDocumento(), new Integer(0))) {
			peticion.setCodTipoDocumento(new Integer(1));
			datos.agregarDato("peticion", peticion);
		}
		
		String codClienteIEMCO = null;
		try {
			codClienteIEMCO = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_IEMCO), String.class, peticion.getCodColector());
			datos.agregarDato("codClienteIEMCO", codClienteIEMCO);
		} catch (EmptyResultDataAccessException erdae) {
			throw new ServicioException(20019, "NO EXISTE COLECTOR", "COLECTOR");
		}
		
		//4.
		AdaptadorDeMapa adaptadorICRDO = null;
		try {
			
			Object[] paramsICRDO = {
				peticion.getCodColector(),
				peticion.getCodTipoDocumento()
			};
			logger.debug("Ejecutando sentencia SELECT_LINC_SFBDB_ICRDO, parametros: {}", Arrays.toString(paramsICRDO));
			Map<String, Object> selectICRDO = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_ICRDO), paramsICRDO);
			adaptadorICRDO 	= UtileriaDeDatos.adaptarMapa(selectICRDO);
			datos.agregarDato("adaptadorICRDO", adaptadorICRDO);
			datos.agregarDato("codCausal", adaptadorICRDO.getInteger("codCausal"));
			datos.agregarDato("codSubCausal", adaptadorICRDO.getInteger("codSubCausal"));
			
		} catch (EmptyResultDataAccessException erdae) {
			throw new ServicioException(20019, "NO EXISTE RELACION TIPO DOCUMENTO CON COLECTOR", "RELACION TIPO DOCUMENTO CON COLECTOR");
		}
		
		//5.
		String codPantalla = datos.obtenerString("codPantalla");
		Integer senSubCausal = null;
		try {
			Integer codCausal = adaptadorICRDO.getInteger("codCausal");
			logger.debug("Ejecutando sentencia SELECT_LINC_SFBDB_AAMTM, parametros: "+codPantalla+" "+codCausal);
			senSubCausal = getJdbcTemplate().queryForObject(query(SELECT_LINC_SFBDB_AAMTM), Integer.class, codPantalla, codCausal);
			
			if (!UtileriaDeDatos.isEquals(senSubCausal, Constantes.SI)) {
				throw new ServicioException(20005, "SENIAL INCORRECTA DE USO SUBCAUSAL EN AAMTM");
			}
			
			datos.agregarDato("senSubCausal", senSubCausal);
		} catch (EmptyResultDataAccessException erdae) {
			throw new ServicioException(20019, "NO EXISTE CODIGO DE CAUSAL/TRANSACCION", "CODIGO DE CAUSAL/TRANSACCION");
		}
		
		//6.
		AdaptadorDeMapa cuentaTransaccionAdaptador = null;
		String cuentaTransaccion = null;
		try {
			Object[] paramsAAMSM = {
				codPantalla,
				adaptadorICRDO.getInteger("codCausal"),
				adaptadorICRDO.getInteger("codSubCausal")
			};
			logger.debug("Ejecutando sentencia SELECT_LINC_SFBDB_AAMSM, parametros: {}", Arrays.toString(paramsAAMSM));
			Map<String, Object> queryForMap = getJdbcTemplate().queryForMap(query(SELECT_LINC_SFBDB_AAMSM), paramsAAMSM);
			cuentaTransaccionAdaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
			datos.agregarDato("cuentaTransaccionAdaptador", cuentaTransaccionAdaptador);
			
			cuentaTransaccion = cuentaTransaccionAdaptador.getString("cuentaStr");

			Integer codProductoCta = cuentaTransaccionAdaptador.getInteger("codProductoCta");
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
			datos.agregarDato("cliente", cliente);
		} catch (EmptyResultDataAccessException erdae) {
			logger.error(NOM_COD_SERVICIO + "NO EXISTE CODIGO DE SUBCAUSAL/TRANSACCION {}", erdae);
			throw new ServicioException(20019, "NO EXISTE CODIGO DE SUBCAUSAL/TRANSACCION", "CODIGO DE SUBCAUSAL/TRANSACCION");
		}
		
	}

	private void seguridadTerminales(DatosOperacion datos) throws TipoDatoException, ServicioException, ParseException {

		ReversaPagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagaduriaGenericoPeticion.class);
		
		datos.agregarDato("codCajero", peticion.getCodCajero());
		datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
		datos.agregarDato("codTran", peticion.getCodTran());
		seguridadTerminalesFinancieros(datos);
		
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		Date fechaSistemaD = UtileriaDeDatos.fecha6ToDate(fechaSistema);
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistemaD);
		
		datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
		datos.agregarDato("codPantalla", Constantes.ISPEC_AJ599);
		datos.agregarDato("senPlanilla", Constantes.NO);
		
	}

	private void validacionInicial(ReversaPagaduriaGenericoPeticion peticion) throws ServicioException {
		//1.
		UtileriaDeParametros.validarParametro(peticion.getCodColector(), "codColector", TipoValidacion.ENTERO_MAYOR_IGUAL, new Integer[] {500});
		UtileriaDeParametros.validarParametro(peticion.getCodTipoDocumento(), "codTipoDocumento", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCamposFormulario(), "camposFormulario", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumReversa(), "numReversa", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getGlbDtime(), "glbDtime", TipoValidacion.LONG_MAYOR_CERO);
		
	}

}
