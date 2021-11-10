package sv.gob.bfa.entradasvarias.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import sv.gob.bfa.core.model.Certificado;
import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.model.CuentaAhorro;
import sv.gob.bfa.core.model.CuentaCorriente;
import sv.gob.bfa.core.model.CuentaPrestamo;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.entradasvarias.funcionesinternas.ReversaInscripcionRenovacionProduceSeguro;
import sv.gob.bfa.entradasvarias.model.ReversaEntradasVariasPeticion;
import sv.gob.bfa.entradasvarias.model.ReversaEntradasVariasRespuesta;

/**
 * Clase contiene la l&oacutegica de negocio correspondiente a la aplicaci&oacute de la reversa
 * de entradas varias AJ201
 */
public class ReversaEntradasVariasServicio extends Servicio{

	private static final String NOM_COD_SERVICIO = "Reversa entradas varias AJ201: ";
	
	private static final String SELECT_LINC_SFBDB_OIMSI = 
			"SELECT OSEADMENV AS senAdmEnvioExterior," + 
			"       OCO_SERVI AS codServicioOIMSI" + 
			"	FROM LINC.SFBDB_OIMSI@DBLINK@" + 
			"	WHERE	OCOTIPSER = ?" + 
			"		AND OCOCAUENV = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_OIMOI = 
			"SELECT OCOTIPSER AS codTipoServicio," + 
			"	ocoperloc as codCliente," + 
			"   ofuperloc as senClientePersona, " +
			"   OCO_SERVI AS codServicioOIMOI," + 
			"   OSEREGBOR AS senRegistroEliminado," + 
			"   OCOTIPOPE AS codTipoOperacion," + 
			"   OCO_ESTAD AS codEstadoRegistro," + 
			"   OVA_APLIC AS valorOperacionInternacional," + 
			"   DCO_OFICI AS codOficinaOIMOI," + 
			"   OCOTIDOLOC AS tipDocumentoLocal," + 
			"   ONUDOCLOC AS numDocumentoLocal," + 
			"   OCOPAIEXT AS codPaisExterno     " + 
			"	FROM LINC.SFBDB_OIMOI@DBLINK@" + 
			"	WHERE ONUOPEINT = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_BSMTG = 
			"SELECT ANO_CORTA AS nombreDocumentoCliente" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE ACO_TABLA = ?" + 
			"	AND aco_codig = LPAD(?, 2, '0')";
			;
	
	private static final String SELECT_LINC_SFBDB_BSMTG_PAIS = 
			"SELECT COUNT(1)" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE ACO_TABLA= ?" + 
			"	AND ACO_CODIG = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_IEACS = 
			"SELECT TVA_MOVIM  AS valorMovimientoIEACS," + 
			"       IFE_TRANS  AS fechaTransaccionIEACS, " + 
			"       ICO_PAGAPL AS codPagoAplicacion," + 
			"       ICO_TIPPAG AS codTipoPago" + 
			"	FROM LINC.SFBDB_IEACS@DBLINK@" + 
			"	WHERE TNUDOCTRA = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_PPRSP = 
			"SELECT PVASUBBFA AS valorSubsidioBFA," + 
			"    	PVA_PRIMA AS valorPrimaSeguro," + 
			"    	PMO_SEGUR AS montoAsegurado," + 
			"    	PSE_RENOV AS senRenovacion," + 
			"    	PFEVENPOL AS fechaVencePoliza," + 
			"    	PFE_SUSCR AS fecSuscripcion," + 
			"    	PFERENPOL AS fecRenuevaPoliza," + 
			"    	PCOCONESP AS codFondoContribEspecial," + 
			"    	PCOPAGSEG AS codPagoSeguro," + 
			"    	GLB_DTIME AS glbDtimePPRSP," + 
			"    	PCOEMPSEG  AS codEmpresaAseguradora" + 
			"	FROM    LINC.SFBDB_PPRSP@DBLINK@" + 
			"	WHERE	PCU_OFICI = ?" + 
			"   	AND PCU_PRODU = ?" + 
			"   	AND PCUNUMCUE = ?" + 
			"   	AND PCOEMPSEG = ?" + 
			"   	AND PCOTIPSEG = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_AAATR = 
			"SELECT GLB_DTIME AS glbDtimeAAATR" + 
			"	FROM LINC.SFBDB_AAATR@DBLINK@" + 
			"	WHERE	TFETRAREL = ?" + 
			"   	AND DCO_OFICI = ?" + 
			"   	AND DCO_TERMI = ?" + 
			"   	AND TNU_TRANS = ?" + 
			"   	AND DCO_TRANS = ?" + 
			"   	AND ACO_CAUSA = ?" + 
			"		AND ACOSUBCAU = ?" +
			"   	AND TNUDOCTRA = ?" + 
			"   	AND TVA_MOVIM = ?" + 
			"   	AND TSE_REVER <> ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_AAATR = 
			"UPDATE LINC.SFBDB_AAATR@DBLINK@" + 
			"   SET TSE_REVER = ?" + 
			" WHERE GLB_DTIME = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_IEACS = 
			"UPDATE LINC.SFBDB_IEACS@DBLINK@" + 
			"   SET ICO_PAGAPL = ?," + 
			"       DCO_OFICI  = ?," + 
			"       DCO_TERMI  = ?," + 
			"       DCO_USUAR  = ?," + 
			"       THO_TRANS  = ?," + 
			"       TFE_PAGO   = ?" + 
			" WHERE TNUDOCTRA  = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_PPRSP = 
			"UPDATE LINC.SFBDB_PPRSP@DBLINK@" + 
			"	SET PFE_SUSCR = ?," + 
			"		PFEVENPOL = ?" + 
			" WHERE GLB_DTIME = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_PPRSP_RENOV = 
			"UPDATE LINC.SFBDB_PPRSP@DBLINK@" + 
			"   SET PFEVENPOL = ?," + 
			"       PFERENPOL = ?" + 
			" WHERE GLB_DTIME = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_OIMOI = 
			"UPDATE LINC.SFBDB_OIMOI@DBLINK@" + 
			"    SET OCO_ESTAD = ?," + 
			"        OCOUSUPRO = ?," + 
			"        OFE_PROCE = ?," + 
			"        OHO_PROCE = ?" + 
			"  WHERE ONUOPEINT = ?"
			;
	
	Logger logger = LoggerFactory.getLogger(ReversaEntradasVariasServicio.class);
	
	/**
	 * M&eacutetodo principal, contiene toda la logica correspondiente a la reversa de entradas varias
	 */
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");
		
		try {
		
			logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion ...");
			DatosOperacion datos = crearDatosOperacion();
			
			logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> ReversaEntradasVariasPeticion");
			ReversaEntradasVariasPeticion peticion = (ReversaEntradasVariasPeticion) objetoDom;
			datos.agregarDato("peticion", peticion);
			datos.agregarPropiedadesDeObjeto(peticion);
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			validadacionInicial(peticion);
			
			//2. Invocacion seguridad
			invocarSeguridad(datos);
			
			//3. Recuperando datos de cuenta y cliente
			recuperarDatosCuentaCliente(datos);
			
			//4. Validar datos operaciones internacionales
			validarDatosOpeInternacionales(datos);
			
			//5. Validar datos prestamos honrados y saneados
			valDatosPrestamosHonrSane(datos);
			
			//6. Valida causal, cuenta ingresada y recupera datos
			//7. Validaciones para la reversa
			//8. Buscar transaccion y actualizar senial de reversa
			validaCausalCuenta(datos);
			
			//9. Actualizar perfiles de transaccion
			//10. Actualiza datos de prestamos relacionados con CARSAN o SEGURO FUTURO
			//11. Inscripcion de seguros
			//12. Renovacion de seguros
			actualizacionesInscRenovSeg(datos);
			
			//13. Actualizar operaciones internacionales
			actualizarOperacionesInternacionales(datos);
			
			//14. Grabar gasto produce seguro
			grabarGastoProduceSeguro(datos);
			
			logger.debug("Preparando objeto respuesta...");
			ReversaEntradasVariasRespuesta respuesta = new ReversaEntradasVariasRespuesta(); 
			datos.agregarDato("valorMovimiento", datos.obtenerBigDecimal("valorEfectivo"));
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			if(logger.isDebugEnabled()) {
				logger.debug("RESPUESTA: {} ", respuesta);
			}
			
			return respuesta;
		} catch (ServicioException e) {
			logger.error("Error de servicio: " + e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(e);	
		} catch (TipoDatoException | ParseException e){
			logger.error("Error inesperado: " + e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
	}
	
	private void grabarGastoProduceSeguro(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaEntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		Integer codCausal = peticion.getCodCausal();
				
		if (UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_PRODUCE_SEGURO)) {
			ReversaInscripcionRenovacionProduceSeguro reversaInscripcion = new ReversaInscripcionRenovacionProduceSeguro(getJdbcTemplate(), getDbLinkValue());
			
			Integer codProductoRecuperado	= datos.obtenerInteger("codProductoRecuperado");
			Integer codOficinaRecuperado	= datos.obtenerInteger("codOficinaRecuperado");
			Integer numCuentaRecuperado		= datos.obtenerInteger("numCuentaRecuperado");
			//Parametros de entrada para funcion interna
			datos.agregarDato("codProducto", codProductoRecuperado);
			datos.agregarDato("codOficina", codOficinaRecuperado);
			datos.agregarDato("numCuenta", numCuentaRecuperado);
//			fechaSistemaAMD
//			peticion.codTerminal
//			peticion.numReversa
//			peticion.numDocumentoTran
//			seg.codPantalla
//			peticion.codCausal
//			peticion.codOficinaTran
			reversaInscripcion.reversarInscripcionRenovacionProduceSeguro(datos);
		}
		
	}

	private void actualizarOperacionesInternacionales(DatosOperacion datos) throws TipoDatoException, ServicioException, ParseException {

		ReversaEntradasVariasPeticion peticion	= datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		Integer senOperacionInternacional	= datos.obtenerInteger("senOperacionInternacional");
		
		if (UtileriaDeDatos.isEquals(senOperacionInternacional, Constantes.SI)) {

			Integer senAdmEnvioExterior			= datos.obtenerInteger("senAdmEnvioExterior");
			Integer codTipoServicio				= Constantes.OI_SERVICIO_REMESAS;
			
			if (!UtileriaDeDatos.isEquals(senAdmEnvioExterior, Constantes.SI)) {
				
				Integer fechaSistema	= datos.obtenerInteger("fechaSistema");
				Date fechaSistemaD 		= UtileriaDeDatos.fecha6ToDate(fechaSistema);
				Calendar calendar		= Calendar.getInstance();
				calendar.setTime(fechaSistemaD);
				Integer anio = calendar.get(Calendar.YEAR);
				Integer mes = calendar.get(Calendar.MONTH)+1;
				Integer dia = calendar.get(Calendar.DAY_OF_MONTH);
				
				datos.agregarDato("codTipoServicio", codTipoServicio);
				datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
				datos.agregarDato("codTipoDocumentoLocal", peticion.getTipDocumentoPersona());
				datos.agregarDato("numDocumentoLocal", peticion.getNumDocumentoPersona());
				datos.agregarDato("anioTran", anio);
				datos.agregarDato("mesTran", mes);
				datos.agregarDato("diaTran", dia);
				datos.agregarDato("senReversa", Constantes.SI);
				acumularSaldoOI(datos);
				
				datos.agregarDato("codTipoServicio", new Integer(0));
				//Los siguientes parametros ya se agregaron en el objeto datos paso anterior
//				datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
//				datos.agregarDato("codTipoDocumentoLocal", peticion.getTipDocumentoPersona());
//				datos.agregarDato("numDocumentoLocal", peticion.getNumDocumentoPersona());
//				datos.agregarDato("anioTran", anio);
//				datos.agregarDato("mesTran", mes);
//				datos.agregarDato("diaTran", dia);
				datos.agregarDato("senReversa", Constantes.SI);
				acumularSaldoOI(datos);
				
			}

			Object[] paramsOIMOI = {
				Constantes.OI_ESTADO_INGRESADO,
				" ",
				new Integer(0),
				new Integer(0),
				peticion.getNumDocumentoTran()
			};
			
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB OIMOI, parametros: {}", Arrays.toString(paramsOIMOI));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_OIMOI), paramsOIMOI);
			
		}
		
		
	}

	private void actualizacionesInscRenovSeg(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		ReversaEntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		
		//9. Se invoca la funcion de actualizacion de perfiles de transaccion
		Long glbDtimeAAATR = datos.obtenerLong("glbDtimeAAATR");
		
		datos.agregarDato("glbDtime", glbDtimeAAATR);
		//codOficinaTran ya esta
		datos.agregarDato("codTerminalTran", peticion.getCodTerminal());
		//fechaRelativa ya esta
		datos.agregarDato("numTran", peticion.getNumReversa());
		//numReversa ya esta
		actualizarPerfilesTransaccionAAATR(datos);
		
		
		//10. Actualiza datos de prestamos relacionados con CARSAN o SEGURO FUTURO
		Integer codCausal = peticion.getCodCausal();
		if (
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_ABONO_EFECTIVO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_CANCELACION_DECRETO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_HONRADO_ABONO_EFECTIVO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_HONRADO_ABONO_FSG)
				) {
			
			Object[] paramsIEACS = {
					Constantes.NO,
					new Integer(0),
					new Integer(0),
					new String(" "),
					new Integer(0),
					new Integer(0),
					peticion.getNumDocumentoTran()
			};
			
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB IEACS, parametros: {}", Arrays.toString(paramsIEACS));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_IEACS), paramsIEACS);
			
		}
		
		//11. Inscricion de seguros 
		
		if (
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_VIDA) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_DANIO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_AUTO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_PRODUCE_SEGURO)
				) {
				
				Object[] paramsPPRSP = {
						new Integer(0),
						new Integer(0),
						datos.obtenerLong("glbDtimePPRSP")
				};
				
				logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB PPRSP, parametros: {}", Arrays.toString(paramsPPRSP));
				ejecutarSentencia(query(UPDATE_LINC_SFBDB_PPRSP), paramsPPRSP);
				
			}
		
		//12. Renovacion de seguros
		
		if (
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_VIDA) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_AUTO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_DANIO)
			) {
			
			Integer fechaVencePolizaAnterior	= new Integer(0);
			Integer fechaVencePoliza			= datos.obtenerInteger("fechaVencePoliza");
			
			if (
				!UtileriaDeDatos.validarFechaAMD(fechaVencePoliza)
				) {
				
				Object[] paramsPPRSP = {
					new Integer(0),
					new Integer(0),
					datos.obtenerLong("glbDtimePPRSP")
				};
				
				logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB PPRSP RENOV, parametros: {}", Arrays.toString(paramsPPRSP));
				ejecutarSentencia(query(UPDATE_LINC_SFBDB_PPRSP_RENOV), paramsPPRSP);
				
			} else {
				
				if (UtileriaDeDatos.anioEsBisiestoDMA(datos.obtenerInteger("fechaSistema"))) {
					Date fechaSistemaD 				= UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
					Date fechaVencePolizaDate		= UtileriaDeDatos.fecha8ToDateyyyyMMdd(fechaVencePoliza);
					Date fechaVencePolizaAnteriorD	= UtileriaDeDatos.restarDiasAFecha(fechaVencePolizaDate, Constantes.PP_ANIO_BISIESTO);
					fechaVencePolizaAnterior 		= UtileriaDeDatos.tofecha8yyyyMMdd(fechaVencePolizaAnteriorD);
				}else {
					Date fechaSistemaD 				= UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
					Date fechaVencePolizaDate		= UtileriaDeDatos.fecha8ToDateyyyyMMdd(fechaVencePoliza);
					Date fechaVencePolizaAnteriorD	= UtileriaDeDatos.restarDiasAFecha(fechaVencePolizaDate, Constantes.PP_ANIO_NO_BISIESTO);
					fechaVencePolizaAnterior 		= UtileriaDeDatos.tofecha8yyyyMMdd(fechaVencePolizaAnteriorD);
				}
				
				Object[] paramsPPRSP = {
						fechaVencePolizaAnterior,
						new Integer(0),
						datos.obtenerLong("glbDtimePPRSP")
					};
					
					logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB PPRSP RENOV, parametros: {}", Arrays.toString(paramsPPRSP));
					ejecutarSentencia(query(UPDATE_LINC_SFBDB_PPRSP_RENOV), paramsPPRSP);
				
			}
			
		}
		
	}

	private void validaCausalCuenta(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaEntradasVariasPeticion peticion	= datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		Integer codCausal 						= peticion.getCodCausal();
		
		if (
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_VIDA) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_VIDA) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_DANIO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_DANIO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_AUTO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_AUTO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_PRODUCE_SEGURO)
			) {
			Integer codConcepto = Constantes.CONCEPTO_PP;
			CuentaPrestamo pcp 	= datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			Integer codTipoSeguro = null;
			//Agregando parametros de entrada para funcion soporte validarEstadoPrestamo
			datos.agregarDato("codEstadoPrestamo", pcp.getCodEstadoPrestamo());
			datos.agregarDato("codBloqueo", pcp.getCodBloqueo());
			
			validarEstadoPrestamos(datos);
			
			if (
				UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_VIDA) || 
				UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_VIDA)
				) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_VIDA;
			}
			
			if (
				UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_DANIO) || 
				UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_DANIO)
				) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_DANIOS;
			}
			
			if (
				UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_INSCRIPCION_AUTO) || 
				UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_RENOV_AUTO)
				) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_AUTO;
			}
			
			if (
				UtileriaDeDatos.isEquals(codCausal, Constantes.PS_CAUSAL_PRODUCE_SEGURO)
				) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_PRODUCE;
			}
			
			//Obteniendo valor a pagar por el cliente...
			try {
				Object[] paramsPPRSP = {
					datos.obtenerInteger("codOficinaRecuperado"),
					datos.obtenerInteger("codProductoRecuperado"),
					datos.obtenerInteger("numCuentaRecuperado"),
					Constantes.PS_SEGURO_FUTURO,
					codTipoSeguro
				};
				logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB PPRSP, parametros: {}", Arrays.toString(paramsPPRSP));
				Map<String, Object> queryForMap = jdbcTemplate.queryForMap(query(SELECT_LINC_SFBDB_PPRSP), paramsPPRSP);
				
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
				
				BigDecimal valorPagaCliente = adaptador.getBigDecimal("valorPrimaSeguro")
														.subtract(adaptador.getBigDecimal("valorSubsidioBFA"));
				
				if (!UtileriaDeDatos.isEquals(valorPagaCliente, peticion.getValorEfectivo())) {
					throw new ServicioException(20018, "VALOR INCORRECTO {}", "DE PRIMA");
				}
				
				datos.agregarDato("glbDtimePPRSP", adaptador.getLong("glbDtimePPRSP"));
				datos.agregarDato("fechaVencePoliza", adaptador.getInteger("fechaVencePoliza"));
				
			} catch (EmptyResultDataAccessException erdae) {
				logger.error(NOM_COD_SERVICIO + "No existe relación de la cuenta con seguros.");
				throw new ServicioException(20019, "No existe {} ", " RELACION DE LA CUENTA CON SEGUROS") ;
			}
		}
			
			//Validaciones para reversa...
			if (
				(UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_ABONO_EFECTIVO) || 
				UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_CANCELACION_DECRETO) || 
				UtileriaDeDatos.isEquals(codCausal, Constantes.PP_HONRADO_ABONO_EFECTIVO) || 
				UtileriaDeDatos.isEquals(codCausal, Constantes.PP_HONRADO_ABONO_FSG)
				) &&
				!UtileriaDeDatos.isEquals(datos.obtenerInteger("codPagoAplicacion"), new Integer(1))
				) {
				logger.error(NOM_COD_SERVICIO + "Estado incorrecto del registro - Orden de pago CARSAN");
				throw new ServicioException(20016, "Estado Incorrecto {} ", " DEL REGISTRO - ORDEN DE PAGO CARSAN") ;
			}
			
			//Buscar transaccion y realizar actualizacion senial reversa
			
			try {
				
				Object[] paramsAAATR = {
						datos.obtenerInteger("fechaRelativa"),
						peticion.getCodOficinaTran(),
						peticion.getCodTerminal(),
						peticion.getNumReversa(),
						peticion.getCodTran(),
						peticion.getCodCausal(),
						peticion.getCodSubcausal(),
						peticion.getNumDocumentoTran(),
						peticion.getValorEfectivo().setScale(2, RoundingMode.HALF_UP),
						Constantes.SI
					};
				
				logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB AAATR: parametros: {}", Arrays.toString(paramsAAATR));
				Long glbDtimeAAATR = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_AAATR), Long.class, paramsAAATR);
				
				datos.agregarDato("glbDtimeAAATR", glbDtimeAAATR);
				
				Object[] paramsAAATRUpdate = {Constantes.SI, glbDtimeAAATR};

				logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB AAATR: parametros: {}", Arrays.toString(paramsAAATRUpdate));
				ejecutarSentencia(query(UPDATE_LINC_SFBDB_AAATR), paramsAAATRUpdate);
				
				
				
			} catch (EmptyResultDataAccessException erdae) {
				logger.error(NOM_COD_SERVICIO + "Transacción no aparece en base de datos.");
				throw new ServicioException(20012, "Transacción no aparece en base de datos.");
			}
			
			
		}
		
	

	private void valDatosPrestamosHonrSane(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaEntradasVariasPeticion peticion	= datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		Integer codCausal						= peticion.getCodCausal();
		
		if (
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_ABONO_EFECTIVO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_CANCELACION_DECRETO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_HONRADO_ABONO_EFECTIVO) || 
			UtileriaDeDatos.isEquals(codCausal, Constantes.PP_HONRADO_ABONO_FSG)
			) {
			
			try {
				Map<String, Object> queryForMap = jdbcTemplate.queryForMap(query(SELECT_LINC_SFBDB_IEACS), peticion.getNumDocumentoTran());
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
				
				if (!UtileriaDeDatos.isEquals(peticion.getValorEfectivo(), adaptador.getBigDecimal("valorMovimientoIEACS"))) {
					logger.error(NOM_COD_SERVICIO + "Valor incorrecto, no coincide con el movimiento CARSAN.");
					throw new ServicioException(20018, "VALOR INCORRECTO {}", 
													"NO COINCIDE CON EL MOVIMIENTO CARSA.");
				}
				
				if (!UtileriaDeDatos.isEquals(datos.obtenerInteger("fechaSistemaAMD"), adaptador.getInteger("fechaTransaccionIEACS"))) {
					logger.error(NOM_COD_SERVICIO + "Fecha incorrecta, no coincide con el movimiento CARSAN.");
					throw new ServicioException(20003, "Fecha incorrecta {}", " NO COINCIDE CON EL MOVIMIENTO CARSAN");
				}
				
				if (UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_CANCELACION_DECRETO) && 
					!UtileriaDeDatos.isEquals(adaptador.getInteger("codTipoPago"), Constantes.PP_PAGO_POR_DECRETO)
					) {
					logger.error(NOM_COD_SERVICIO + "Codigo de causa incorrecto, debe ser cancelacion por decreto.");
					throw new ServicioException(20282, "Código de causa incorrecto {} ", "DEBE SER CANCELACION POR DECRETO") ;
				}
				
				if (UtileriaDeDatos.isEquals(codCausal, Constantes.PP_CARSAN_ABONO_EFECTIVO) && 
						!UtileriaDeDatos.isEquals(adaptador.getInteger("codTipoPago"), Constantes.PP_PAGO_NORMAL)
						) {
						logger.error(NOM_COD_SERVICIO + "Codigo de causa incorrecto, debe ser abono en efectivo.");
						throw new ServicioException(20282, "Código de causa incorrecto {} ", "DEBE SER ABONO EN EFECTIVO") ;
					}
				
				datos.agregarDato("codPagoAplicacion", adaptador.getInteger("codPagoAplicacion"));
				
			} catch (EmptyResultDataAccessException erdae) {
				logger.error(NOM_COD_SERVICIO + "Transaccion no aparece en BD – CARSAN.");
				throw new ServicioException(20212, "Transaccion no aparece en BD – CARSAN.");
			}
			
		}
		
	}

	private void validarDatosOpeInternacionales(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaEntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		Integer senOperacionInternacional = Constantes.NO;
		String codCliente = null;
		
		try {
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELEC LINC SFBDB OIMSI, parametros: codCausal: {}", peticion.getCodCausal());
			Map<String, Object> queryForMap = jdbcTemplate.queryForMap(query(SELECT_LINC_SFBDB_OIMSI), Constantes.OI_SERVICIO_REMESAS, peticion.getCodCausal());
			
			senOperacionInternacional = Constantes.SI;
			
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
			datos.agregarDato("senAdmEnvioExterior", adaptador.getInteger("senAdmEnvioExterior"));
			datos.agregarDato("codServicioOIMSI", adaptador.getInteger("codServicioOIMSI"));
			
		} catch (EmptyResultDataAccessException ignored) {
		}
		
		if (
			(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.OI_CAUSAL_INGRESO_REMESA_RIA) || 
			 UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.OI_CAUSAL_INGRESO_REMESA_MONEY_GRAM)
			) && 
			!UtileriaDeDatos.isEquals(senOperacionInternacional, Constantes.SI)
			) {
			logger.error(NOM_COD_SERVICIO + "Codigo de causal/transaccion incompatibles");
			throw new ServicioException(20220, "Codigo de causal/transaccion incompatibles.");
		}
		
		if (UtileriaDeDatos.isEquals(senOperacionInternacional, Constantes.SI)) {
			
			if (UtileriaDeDatos.isEquals(datos.obtenerInteger("senAdmEnvioExterior"), Constantes.SI)) {
				logger.error(NOM_COD_SERVICIO + "Tipo de operación inválida – señal administrativa");
				throw new ServicioException(20108, "Tipo de Operacion Invalida {} ", " SEÑAL ADMINISTRATIVA");
			}
			
			try {
				logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB OIMOI, parametros: numDocumentoTran: {}", peticion.getNumDocumentoTran());
				Map<String, Object> queryForMap = jdbcTemplate.queryForMap(query(SELECT_LINC_SFBDB_OIMOI), peticion.getNumDocumentoTran());
				
				AdaptadorDeMapa opInter = UtileriaDeDatos.adaptarMapa(queryForMap);
				
				if (UtileriaDeDatos.isEquals(opInter.getInteger("senClientePersona"),1)) {
					codCliente = opInter.getString("codCliente");
					Cliente cliente = recuperarDatosCliente(codCliente);
					datos.agregarDato("cliente", cliente);
					datos.agregarPropiedadesDeObjeto(cliente);
					datos.agregarDato("nombreCompletoCliente", datos.obtenerValor("nombreModificadoCliente"));
				}
				if (!UtileriaDeDatos.isEquals(opInter.getInteger("codTipoServicio"), Constantes.OI_SERVICIO_REMESAS)) {
					logger.error(NOM_COD_SERVICIO + "Numero incorrecto – Op. Internacional no es remesa");
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. INTERNACIONAL NO ES REMESA");
				}
				
				if (!UtileriaDeDatos.isEquals(opInter.getInteger("codServicioOIMOI"), datos.obtenerInteger("codServicioOIMSI"))) {
					logger.error(NOM_COD_SERVICIO + "Numero incorrecto – Op. Internacional y Causal");
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. INTERNACIONAL Y CAUSAL");
				}
				
				if (UtileriaDeDatos.isEquals(opInter.getInteger("senRegistroEliminado"), Constantes.SI)) {
					logger.error(NOM_COD_SERVICIO + "Numero incorrecto – Op. Internacional ya fue eliminada");
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. INTERNACIONAL YA FUE ELIMINADA");
				}
				
				if (!UtileriaDeDatos.isEquals(opInter.getString("codTipoOperacion"), Constantes.OI_ENVIO)) {
					logger.error(NOM_COD_SERVICIO + "Tipo de operación inválida – Op. Internacional.");
					throw new ServicioException(20108, "Tipo de Operacion Invalida {} ", " OP. INTERNACIONAL");
				}
				
				if (UtileriaDeDatos.isEquals(opInter.getString("codEstadoRegistro"), Constantes.OI_ESTADO_INGRESADO)) {
					logger.error(NOM_COD_SERVICIO + "Estado incorrecto – Op. Internacional sin procesar.");
					throw new ServicioException(20016, "Estado Incorrecto {} ", " OP. INTERNACIONAL SIN PROCESAR") ;
				}
				
				if (!UtileriaDeDatos.isEquals(opInter.getString("codEstadoRegistro"), Constantes.OI_ESTADO_PROCESADO)) {
					logger.error(NOM_COD_SERVICIO + "Estado incorrecto – Op. Internacional.");
					throw new ServicioException(20016, "Estado Incorrecto {} ", " OP. INTERNACIONAL.") ;
				}
				
				if (!UtileriaDeDatos.isEquals(peticion.getValorEfectivo(), opInter.getBigDecimal("valorOperacionInternacional"))) {
					logger.error(NOM_COD_SERVICIO + "Valor de la transacción incorrecta.");
					throw new ServicioException(20224, "Valor de la transacción incorrecta.");
				}
				
				if (!UtileriaDeDatos.isEquals(opInter.getInteger("codOficinaOIMOI"), peticion.getCodOficinaTran())) {
					logger.error(NOM_COD_SERVICIO + "Numero incorrecto – Op. es de otra agencia.");
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. ES DE OTRA AGENCIA");
				}
				
				String nombreDocumentoCliente = "";
				try {
					//Tipo documento valido
					nombreDocumentoCliente = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_BSMTG), String.class, 
																				"DOC-VIGFIN", peticion.getTipDocumentoPersona().toString());
					datos.agregarDato("nombreDocumentoCliente", nombreDocumentoCliente);
				} catch (EmptyResultDataAccessException erdae) {
					logger.error(NOM_COD_SERVICIO + "No existe – Tipo de documento.");
					throw new ServicioException(20019, "No existe {} ", " TIPO DE DOCUMENTO") ;

				}
				
				if (
					!UtileriaDeDatos.isEquals(peticion.getTipDocumentoPersona(), opInter.getInteger("tipDocumentoLocal")) ||
					!UtileriaDeDatos.isEquals(peticion.getNumDocumentoPersona(), opInter.getString("numDocumentoLocal"))
					) {
					logger.error(NOM_COD_SERVICIO + "Numero incorrecto – Documento es diferente.");
					throw new ServicioException(20589, "Numero incorrecto {}", "DOCUMENTO ES DIFERENTE");
				}
				
				if (UtileriaDeDatos.isGreaterThanZero(opInter.getInteger("codPaisExterno"))) {
					try {
						logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB BSMTG PAIS, parametros: codPaisExterno: {}", 
																										opInter.getInteger("codPaisExterno"));
						Integer count = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_BSMTG_PAIS), Integer.class, 
																	"PAISES", opInter.getInteger("codPaisExterno").toString());
						if (UtileriaDeDatos.isEquals(count, new Integer(0))) {
							logger.error(NOM_COD_SERVICIO + "TIPO DE TABLA NO EXISTE – PAIS ORIGEN REMITENTE.");
							throw new ServicioException(20291, "TIPO DE TABLA NO EXISTE {} ", "- PAIS ORIGEN REMITENTE") ;
						}
					} catch (EmptyResultDataAccessException erdae) {
						logger.error(NOM_COD_SERVICIO + "Tipo de tabla no existe – Pais origen remitente.");
						throw new ServicioException(20291, "Tipo de tabla no existe – Pais origen remitente.");
					}
				}
				
				
			} catch (EmptyResultDataAccessException e) {
				logger.error(NOM_COD_SERVICIO + "No existe – documento en operación internacional");
				throw new ServicioException(20019, "No existe {} ", " DOCUMENTO EN OPERACION INTERNACIONAL");
			}
			
		}
		
		datos.agregarDato("senOperacionInternacional", senOperacionInternacional);
		
	}

	private void recuperarDatosCuentaCliente(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaEntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		
		Integer codProducto = datos.obtenerInteger("codProducto");
		Integer conceptoCuenta = datos.obtenerInteger("conceptoCuenta");
		String cuentaTransaccion = datos.obtenerString("cuentaTransaccion");
		
		Integer codOficinaRecuperado		= null;
		Integer codProductoRecuperado		= null;
		Integer numCuentaRecuperado			= null;
		Integer digitoVerificadorRecuperado	= null;
		
		Cliente cliente = null;
		
		if (
			!UtileriaDeDatos.isNull(peticion.getCuentaTransaccion()) && 
		    !UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion()) &&
			!UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) &&
		    !UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO)
			) {
			switch (conceptoCuenta) {
				case 1:
					CuentaCorriente pcc			= recuperarDatosCuentaCorriente(cuentaTransaccion);
					cliente						= recuperarDatosCliente(pcc.getCodCliente());
					codOficinaRecuperado		= pcc.getCodOficina();
					codProductoRecuperado		= pcc.getCodProducto();
					numCuentaRecuperado			= pcc.getNumCuenta();
					digitoVerificadorRecuperado	= pcc.getDigitoVerificador();
					datos.agregarDato("pcc", pcc);
					break;
				case 2:
					CuentaAhorro pca			= recuperarDatosCuentaAhorro(cuentaTransaccion);
					cliente						= recuperarDatosCliente(pca.getCodCliente());
					codOficinaRecuperado		= pca.getCodOficina();
					codProductoRecuperado		= pca.getCodProducto();
					numCuentaRecuperado			= pca.getNumCuenta();
					digitoVerificadorRecuperado	= pca.getDigitoVerificador();
					datos.agregarDato("pca", pca);
					break;
				case 4:
					Certificado pce				= recuperarDatosCuentaCertificado(cuentaTransaccion);
					cliente						= recuperarDatosCliente(pce.getCodCliente());
					codOficinaRecuperado		= pce.getCodOficina();
					codProductoRecuperado		= pce.getCodProducto();
					numCuentaRecuperado			= pce.getNumCuenta();
					digitoVerificadorRecuperado	= pce.getDigitoVerificador();
					datos.agregarDato("pce", pce);
					break;
				case 6:
					CuentaPrestamo pcp			= recuperarDatosCuentaPrestamo(cuentaTransaccion);
					cliente						= recuperarDatosCliente(pcp.getCodCliente());
					codOficinaRecuperado		= pcp.getCodOficina();
					codProductoRecuperado		= pcp.getCodProducto();
					numCuentaRecuperado			= pcp.getNumCuenta();
					digitoVerificadorRecuperado	= pcp.getDigitoVerificador();
					datos.agregarDato("pcp", pcp);
					break;
	
				default:
					break;
			}
			
			if (!UtileriaDeDatos.isNull(cliente)) {
				datos.agregarPropiedadesDeObjeto(cliente);
			}
			
			datos.agregarDato("codOficinaRecuperado", codOficinaRecuperado);
			datos.agregarDato("codProductoRecuperado", codProductoRecuperado);
			datos.agregarDato("numCuentaRecuperado", numCuentaRecuperado);
			datos.agregarDato("digitoVerificadorRecuperado", digitoVerificadorRecuperado);
			datos.agregarDato("cliente", cliente);
		}
		
	}

	private void invocarSeguridad(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		ReversaEntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", ReversaEntradasVariasPeticion.class);
		String numTransaccion = peticion.getCuentaTransaccion();
		Integer codProducto = new Integer(0);
		Integer conceptoCuenta = new Integer(0);
		
		if (!UtileriaDeDatos.isNull(numTransaccion) && !UtileriaDeDatos.isBlank(numTransaccion) &&
				!UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) &&
			    !UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO)
			) {
			String codProductoStr = numTransaccion.substring(0, 3);
			String codConceptoStr = numTransaccion.substring(0,1);
			conceptoCuenta = Integer.valueOf(codConceptoStr);
			codProducto = Integer.valueOf(codProductoStr);
		}
		
		datos.agregarDato("codProducto", codProducto);
		datos.agregarDato("conceptoCuenta", conceptoCuenta);
		logger.debug(NOM_COD_SERVICIO + "Invocacion de funcion de soporte seguridad terminales financieros...");
		seguridadTerminalesFinancieros(datos);
		
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		Date fechaSistemaDate = UtileriaDeDatos.fecha6ToDate(fechaSistema);
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistemaDate);
		datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
		
	}

	/**
	 * M&eacutetodo para realizar validaciones iniciales sobre los par&aacutemetros recibidos.
	 * @param peticion
	 * @throws ServicioException
	 */
	private void validadacionInicial(ReversaEntradasVariasPeticion peticion) throws ServicioException {
		logger.debug(NOM_COD_SERVICIO + "Iniciando validacion de parametros");
		logger.debug(NOM_COD_SERVICIO + "Peticion recibida: {}", peticion);
		
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCausal(), "codCausal", TipoValidacion.ENTERO_MAYOR_CERO);
		if(!UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion())) {
			UtileriaDeParametros.validarParametro(peticion.getCuentaTransaccion(), "cuentaTransaccion", TipoValidacion.CADENA_NUMERICA);
		}
		UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumReversa(), "numReversa", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodSubcausal(), "codSubcausal", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
	}
	
}
