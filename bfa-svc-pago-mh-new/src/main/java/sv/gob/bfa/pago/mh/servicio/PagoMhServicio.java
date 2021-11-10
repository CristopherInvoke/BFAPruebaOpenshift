package sv.gob.bfa.pago.mh.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import sv.gob.bfa.core.fs.FSAuxiliares;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.pago.mh.model.PagoMhPeticion;
import sv.gob.bfa.pago.mh.model.PagoMhRespuesta;


/**
 * Clase contiene logica del negocio correspondiente a Pago Ministerio de Hacienda
 */
public class PagoMhServicio extends Servicio{
	
	private PlatformTransactionManager platformTransactionManager;
//	private final static String CODIGO_BANCO = "09";
//	private final static String MEDIO_PAGO = "02";
	private final static String COD_LOCALIZACION_NPE = "0463";
	private final static String COD_LOCALIZACION_CODBARRA = "7419700004639";
	
	private final static String SELECT_SFBDB_AAMTM = 
			  " SELECT 1 AS valor FROM LINC.SFBDB_AAMTM@DBLINK@ "
			+ " WHERE DCO_ISPEC  = ? AND ACO_CAUSA = ?";
	
	//Query para recuperar numero de transaccion
	private static String SELECT_FNC_CORREL_CANAL = "SELECT MADMIN.FNC_CORREL_CANAL(?) as numTran FROM DUAL" ;
	
	private static String SELECT_GLBDTIME_DIF = "SELECT MADMIN.GENERATE_GLBDTIME_DIF as glbDtimeDAALA FROM DUAL";
	
	private static String INSERT_SFBDB_IEACH = "INSERT INTO LINC.SFBDB_IEACH@DBLINK@ " +
			" (GLB_DTIME, " +
			" ICO_BARRA,  " +
			" INU_NPE,  " +        
			" INU_REGIS,  " +
			" DCO_OFICI,  " +
			" DCO_TERMI, " +   
			" DCO_USUAR, " +
			" ICO_ESTAD, " +
			" ICO_RESMH,  " +     //AAAAMMDD 
			" IFE_PAGO,  " +
			" IFE_REGIS,  " +
			" IFE_VENCI,  " +    
			" IHO_PAGO, " +
			" IHO_REGIS, " +
			" IMO_PAGO, " +
			" TNUDOCTRA, " +
			" IDE_RESMH, " +
			" INU_TRAMH " +
			" ) "           +
		    " VALUES ("      + 
				" ?, ?, ?, ?, ?, "  +
			    " ?, ?, ?, ?, ?, "  +
			    " ?, ?, ?, ?, ?,  " +
			    " ?, ?, ? " +			    
			
			" ) ";
	
	private final static String SELECT_SFBDB_IEACH = 
			  " SELECT 1 AS valor FROM LINC.SFBDB_IEACH@DBLINK@ "
			+ " WHERE ICO_BARRA = ? "
			+ " AND INU_NPE = ?" 
			+ " AND ICO_ESTAD IN (?,?,?,?,?)";
	
	private final static String SELECT_SFBDB_IEACH3 = 
			  " SELECT INU_REGIS AS numRegis FROM LINC.SFBDB_IEACH@DBLINK@ "
			+ " WHERE ICO_BARRA = ? "
			+ " AND IFE_REGIS = ?" 
			+ " AND ICO_ESTAD = ?"
			+ " AND ICO_RESMH = ?"
			;
	
	private static String SELECT_SFBDB_AAMTC = "SELECT ANU_FOLI8 AS folio" +
			"   FROM LINC.SFBDB_AAMTC@DBLINK@" + 
            "   WHERE ACOTIPCOR = ?"
			;
	
	private static String UPDATE_SFBDB_AAMTC = "UPDATE LINC.SFBDB_AAMTC@DBLINK@" + 
			"	SET ANU_FOLI8 = ?" +
			"	WHERE ACOTIPCOR = ?" 
			;
	
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
	
	private final static String SELECT_SFBDB_DXMTR = 
			  " SELECT DCO_ISPEC AS dcoIspec FROM LINC.SFBDB_DXMTR@DBLINK@ "
			+ " WHERE DCO_TRANS  = ? ";
	
	Logger logger = LoggerFactory.getLogger(PagoMhServicio.class);	

	private Long correlativo;
	private LogColector logColector;
	
	@Transactional("transactionManager")
	@Override
	public Object procesar(Object objetoDom) throws ServicioException  {	
		
		
	    DefaultTransactionDefinition paramTransactionDefinition = new    DefaultTransactionDefinition();
        TransactionStatus status=platformTransactionManager.getTransaction(paramTransactionDefinition );
        
		logger.debug("Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();

		logger.debug("Cast de objeto de dominio -> PagoMhPeticion ");
		PagoMhPeticion peticion = (PagoMhPeticion) objetoDom;
		
		
	


		try {	
			
			logger.debug("Obteniendo correlativo ...");
			validacionCorrelativo(datos);
			logColector = new LogColector(2, Integer.valueOf(datos.obtenerInteger("inuRegis")), objetoDom, new FSAuxiliares(jdbcTemplate, getDbLinkValue()), getDbLinkValue());
			
			logColector.registrar( "Iniciando Pago", "");
			
			logger.debug("Validando parametros iniciales ...");
			validacionParametrosIniciales(peticion);
			
			logColector.registrar( "Iniciando Pago", "");
			logger.debug("Iniciando agregando propiedades del objeto al mapa ");
			datos.agregarPropiedadesDeObjeto(peticion);
			datos.agregarDato("peticion", peticion);
			
			logColector.registrar( "Validando la seguridad de terminales", "");
			logger.debug("Invocando la funcion de soporte 'Seguridad para Terminales financieros' para obtener datos como fecha de sistema ...");
			seguridadTerminalesFinancieros(datos);				
			
			logColector.registrar( "Desglosando codigo de barra o npe ", "");
			logger.debug("Desglosando codigo de barra o npe");
			desglosarCodBarraNpe(datos);
			
			logger.debug("Validando datos de npe ...");
			logColector.registrar( "Validando datos NPE", "");
			validarDatosCodBarraNpe(datos);   
			
			logColector.registrar( "Verificanado, si existe registro en la tabla de control", "");
			logger.debug("Validando existencia de registro en SFBDB_IEACH ...");
			validacionExistenciaRegistro(datos); 	
			
			
			logger.debug("Validando existencia de registro respuesta WS en SFBDB_IEACH ...");
//			validacionRegistroRespuesta(datos);
			

			logger.debug("Validando codTran ...");
			validacionTransaccionObtenerNombreIspec(datos);
			

			logColector.registrar( "Verificando Causal", "");

			logger.debug("Validando causal ...");
			validacionTransaccionCausal(datos);
			
			
			
			logger.debug("Insertando registro de la transaccion en el tanque de transacciones ...");
			logColector.registrar( "Registrando en tabla maestra de transacciones(AAATR)", "");
			registroTransaccion(datos);
			
			logger.debug("Insertando registro de la transaccion en la tabla de control SFBDB_IEACH ...");
			logColector.registrar("Registrando en tabla maestra de la tabla de control(IEACH)", "");
			insertarRegistroPagoTransacc(datos);
			
			PagoMhRespuesta pagoMhRespuesta = new PagoMhRespuesta();
			
			datos.llenarObjeto(pagoMhRespuesta);
			
			pagoMhRespuesta.setCodigo(0);
			pagoMhRespuesta.setDescripcion("EXITO");
			pagoMhRespuesta.setIdTransaccionMh(datos.obtenerInteger("inuRegis"));
			
			logColector.registrar( "Pago registrado existosamente en la tabla mestra de transaccion y tabla de control", "json:"+ pagoMhRespuesta);
			logColector.registrar( "En espera de procesamiento del Monitor", "---");
			
			logger.debug("Respuesta de invocacion Pago MMHH NPE (AJ317): " + pagoMhRespuesta);


			platformTransactionManager.commit(status);
			return pagoMhRespuesta;
		
		}catch (ServicioException e) {
			logger.error("Ocurrio un error:", e.getMessage(), e);
			logColector.registrar( "Problema realizar el pago, se abortora", e.getMessage());
			
			throw manejarMensajeExcepcionServicio(e);
		}catch(TipoDatoException | ParseException e) {
			platformTransactionManager.rollback(status);
			logger.error("Ocurrio un error inesperado al ingresar pago:", e.getMessage(), e);
			logColector.registrar( "Problema realizar el pago, se abortora", e.getMessage());
			throw manejarMensajeExcepcionServicio(new ServicioException(20001,"Error inesperado al ingresar pago: "+e.getMessage()));
		}
		
	}
	
	
	/**
	 * M&eacutetodo para validar parametros iniciales 
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionParametrosIniciales(PagoMhPeticion peticion) throws ServicioException {
		
		UtileriaDeParametros.validarParametro(peticion.getCodBarraNpe(), "codBarraNpe", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getCodCausal(), "codCausal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodSubCausal(), "codCausal", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);                    
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminalTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);
		
		if (UtileriaDeDatos.isGreater(peticion.getCodBarraNpe().length(),54) ||
				UtileriaDeDatos.lessThan(peticion.getCodBarraNpe().length(),34)) {
			logger.error("Longitud incorrecta de codigo de barra/npe",peticion.getCodBarraNpe());
			throw new ServicioException(20018,"VALOR INCORRECTO -  {}", "LONGITUD INCORRECTA DE CÓDIGO DE BARRA/NPE") ;
		}
	   
		if (UtileriaDeDatos.isGreater(peticion.getCodBarraNpe().length(),34) 
				& UtileriaDeDatos.lessThan(peticion.getCodBarraNpe().length(),54)) {
			logger.error("Longitud incorrecta de código de barra/npe",peticion.getCodBarraNpe());
			throw new ServicioException(20018,"VALOR INCORRECTO -  {}", "LONGITUD INCORRECTA DE CÓDIGO DE BARRA/NPE");
		}
	}
	
	/**
	 * Método utilizado para parsear el código del comprobante, sea código de barra o NPE 
	 * @param datos
	 * @throws TipoDatoException
	 */
	private void desglosarCodBarraNpe(DatosOperacion datos) throws ServicioException {

		try {
			PagoMhPeticion peticion = datos.obtenerObjeto("peticion", PagoMhPeticion.class);
			this.logColector.registrar("Valor a desglosar"+peticion.getCodBarraNpe(), "");
			String codBarraNpe = peticion.getCodBarraNpe();
			String montoEntero;
			String montoDecimal;
			String fechaVencimiento;
			String tnudoctra;
			String origenPago;
			String codigo = null;
			String comodin = "0";
			String digitoVerificador = "0";
			String codBarra;
			String npe;

			if (UtileriaDeDatos.isEquals(codBarraNpe.length(), 34)) {

				String prefCodLoc = "415";
				String prefCantPagar = "3902";
				String prefFechaVenc = "96";
				String prefRefPago = "8020";
				String referencia = codBarraNpe.substring(23, 33);

				montoEntero = codBarraNpe.substring(4, 12);
				montoDecimal = codBarraNpe.substring(12, 14);
				fechaVencimiento = codBarraNpe.substring(14, 22);
				tnudoctra = codBarraNpe.substring(25, 33);
				origenPago = codBarraNpe.substring(23, 25);
				;

				codBarra = prefCodLoc + COD_LOCALIZACION_CODBARRA + prefCantPagar + montoEntero + montoDecimal
						+ prefFechaVenc + fechaVencimiento + prefRefPago + referencia;

				codigo = "N";
				datos.agregarDato("codigo", codigo);
				datos.agregarDato("codLocalizacion", COD_LOCALIZACION_NPE);
				datos.agregarDato("desgNpe", codBarraNpe);
				datos.agregarDato("desgCodBarra", codBarra);

				datos.agregarDato("codBarra", codBarra);
				datos.agregarDato("npe", codBarraNpe);
			} else {
				if (UtileriaDeDatos.isEquals(codBarraNpe.length(), 54)) {
					montoEntero = codBarraNpe.substring(20, 28);
					montoDecimal = codBarraNpe.substring(28, 30);
					fechaVencimiento = codBarraNpe.substring(32, 40);
					tnudoctra = codBarraNpe.substring(46, 54);
					origenPago = codBarraNpe.substring(44, 46);

					npe = COD_LOCALIZACION_NPE + montoEntero + montoDecimal + fechaVencimiento + comodin + origenPago
							+ tnudoctra + digitoVerificador;
					String npeAux = COD_LOCALIZACION_NPE + montoEntero + montoDecimal + fechaVencimiento + comodin
							+ origenPago + tnudoctra;
					logger.debug("Ejecutando invocacion calcularDigitoVerif(npe) npe: " + npeAux);
					digitoVerificador = calcularDigitoVerif(npe).toString();
					// npe =
					// COD_LOCALIZACION_NPE+montoEntero+montoDecimal+fechaVencimiento+comodin+origenPago+tnudoctra+digitoVerificador;
					npe = npeAux + digitoVerificador;

					codigo = "B";
					datos.agregarDato("codigo", codigo);
					datos.agregarDato("codLocalizacion", COD_LOCALIZACION_CODBARRA);
					datos.agregarDato("desgNpe", npe);
					datos.agregarDato("desgCodBarra", codBarraNpe);

					datos.agregarDato("codBarra", codBarraNpe);
					datos.agregarDato("npe", npe);
				} else {
					throw new ServicioException(20018, "VALOR INCORRECTO -  {}",
							"LONGITUD INCORRECTA DE CÓDIGO DE BARRA/NPE");
				}
			}

			datos.agregarDato("montoPagar", new BigDecimal(montoEntero + "." + montoDecimal));
			datos.agregarDato("fechaVencimiento", Integer.parseInt(fechaVencimiento));
			datos.agregarDato("tnudoctra", Integer.parseInt(tnudoctra));
			datos.agregarDato("origenPago", origenPago);

		} catch (TipoDatoException e) {
			logger.error("Error recuperando objetos {}", e.getMessage(), e);
			throw new ServicioException(20010, "Error recuperando objetos auxiliares");
		}
		
	}
	
	/**
	 * Metodo para validar datos de codigo de barra y npe
	 * @param datos
	 * @throws ServicioException, TipoDatoException,ParseException
	 */
	private void validarDatosCodBarraNpe(DatosOperacion datos) throws ServicioException {
		try {
			PagoMhPeticion peticion = datos.obtenerObjeto("peticion", PagoMhPeticion.class);
			BigDecimal montoPagar = datos.obtenerBigDecimal("montoPagar");
			Integer fechaVencimiento = datos.obtenerInteger("fechaVencimiento");
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			Date fechaSistemaDate = UtileriaDeDatos.fecha6ToDate(fechaSistema);
			Date fechaVencimientoDate = UtileriaDeDatos.fecha8ToDateyyyyMMdd(fechaVencimiento);
			String codigo = datos.obtenerString("codigo");

			UtileriaDeParametros.validarParametro(montoPagar, "montoPagar", TipoValidacion.BIGDECIMAL_MAYOR_CERO);

			if (fechaSistemaDate.after(fechaVencimientoDate)) {
				logger.error("Fecha actual es mayor a la fecha de vencimiento");
				throw new ServicioException(20018, "Valor incorrecto {}",
						" FECHA ACTUAL ES MAYOR A LA FECHA DE VENCIMIENTO");
			}

			if (UtileriaDeDatos.isEquals(codigo, "N")) {
				String codLocalNpe = datos.obtenerString("codLocalizacion");
				if (!UtileriaDeDatos.isEquals(codLocalNpe, COD_LOCALIZACION_NPE)) {
					logger.error("Error en codigo de localizacion npe");
					throw new ServicioException(20018, "VALOR INCORRECTO -  {}",
							" ERROR EN CÓDIGO DE LOCALIZACIÓN NPE");
				}

				// if (!ValidarDigito(peticion.getCodBarraNpe())) {
				if (!validarDigitoVerif(peticion.getCodBarraNpe())) {
					logger.error("Error en digito verificador npe");
					throw new ServicioException(20018, "VALOR INCORRECTO -  {}", " ERROR EN DIG. VERIFICADOR NPE");
				}
			}

			if (UtileriaDeDatos.isEquals(codigo, "B")) {
				String codLocalNpe = datos.obtenerString("codLocalizacion");
				if (!UtileriaDeDatos.isEquals(codLocalNpe, COD_LOCALIZACION_CODBARRA)) {
					logger.error("Error en código de localizacion código de barra");
					throw new ServicioException(20018, "VALOR INCORRECTO -  {}",
							" ERROR EN CÓDIGO DE LOCALIZACIÓN COD BARRA");
				}
			}
			int i = fechaSistemaDate.compareTo(new Date());
//			if(!UtileriaDeDatos.isEquals(i, new Integer(0))) {
//				logger.error("Error en fecha de sistema");
//				throw new ServicioException(20018, "VALOR INCORRECTO -  {}",
//						" FECHA ACTUAL");				
//			}

		} catch (TipoDatoException | ParseException e) {
			logger.error("Error recuperando objetos {}", e.getMessage(), e);
			throw new ServicioException(20010, "Error recuperando objetos auxiliares");
		}
	}
	
	/**
	 * Método utilizado para validar la existencia del registro en la tabla de control, con estado ZE (solicitud envio) o PA (pagado)
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionExistenciaRegistro(DatosOperacion datos) throws ServicioException{		

			List<Map<String, Object>> resultado = null;
			
			Object[] paramsIEACH = { 
					datos.obtenerValor("desgCodBarra") 
					,datos.obtenerValor("desgNpe")
					,"PA" //"PAA"
					,"ZE" //"ZEE"
					,"RR"
					,"RD"
					,"RE"
					};
			
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEACH, parametros: " + Arrays.toString(paramsIEACH));
			try {
				resultado = jdbcTemplate.queryForList(query(SELECT_SFBDB_IEACH), paramsIEACH);
			} catch (EmptyResultDataAccessException ignored) {
				logger.debug("NO EXISTE");
			}
			
			if (!UtileriaDeDatos.isEquals(resultado.size(), new Integer(0))) {
				throw new ServicioException(20020, "YA EXISTE - {}", "CODIGO DE BARRA/NPE DUPLICADO");
			}

	}	
	
	/**
	 * Método utilizado para validar la existencia del registro en la tabla de control, con código de error = 1
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	@Deprecated
	private void validacionRegistroRespuesta(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException{		

		List<Map<String, Object>> resultado = null;
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
		Integer numRegis = new Integer(0);
			Object[] paramsIEACH = { 
					datos.obtenerValor("desgCodBarra") //peticion.getCodBarraNpe()
					,fechaSistemaAMD
					,"RE" 
					,Constantes.SI
					};
			
		logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEACH, parametros: " + paramsIEACH.toString());
		try {
			resultado = jdbcTemplate.queryForList(query(SELECT_SFBDB_IEACH3), paramsIEACH);
			AdaptadorDeMapa adaptador = null;
			for (Map<String, Object> m : resultado) {
				adaptador = UtileriaDeDatos.adaptarMapa(m);
				numRegis = adaptador.getInteger("numRegis");
			}
		} catch (EmptyResultDataAccessException ignored) {

		}
			
		if (!UtileriaDeDatos.listIsEmptyOrNull(resultado)) {
			throw new ServicioException(20020, "YA EXISTE - {}", "REGISTRO ENCONTRADO" + numRegis);
		}

	}
	
	/**
	 * M&eacutetodo para validar la relacion codTransaccion-nombreIspec del codTran obtenido de la peticion
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionTransaccionObtenerNombreIspec(DatosOperacion datos) throws ServicioException{
		
		try {
			Map<String, Object> resultado = null;
			PagoMhPeticion peticion= datos.obtenerObjeto("peticion", PagoMhPeticion.class);
			
			Object [] paramsDXMTR = {					
					peticion.getCodTran()			
			};
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB DXMTR, parametros: " + Arrays.toString(paramsDXMTR));
			try {
				resultado = jdbcTemplate.queryForMap(query(SELECT_SFBDB_DXMTR), paramsDXMTR);
			} catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20019, "NO EXISTE {}", "NOMBRE DE TRANSACCION RELACIONADO A LA TRANSACCION");
			}
			
			if(UtileriaDeDatos.isNull(resultado)) {
				throw new ServicioException(20019, "NO EXISTE {}", "NOMBRE DE TRANSACCION RELACIONADO A LA TRANSACCION");
			}	
			
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(resultado);
			String dcoIspec = adaptador.getString("dcoIspec").trim();
			datos.agregarDato("dcoIspec", dcoIspec);
			
		} catch (TipoDatoException e) {
			logger.error("Error recuperando objetos {}", e.getMessage(), e);
			throw new ServicioException(20010, "Error recuperando objetos auxiliares");
		}

	}
	
	/**
	 * M&eacutetodo para validar la relacion transaccion-causal del causal obtenido de la peticion
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionTransaccionCausal(DatosOperacion datos) throws ServicioException{
		
		try {
			Map<String, Object> resultado = null;
			PagoMhPeticion peticion= datos.obtenerObjeto("peticion", PagoMhPeticion.class);
			
			Object [] paramsAAMTM = {
					datos.obtenerString("dcoIspec") // "AJ317"
					,peticion.getCodCausal()			
			};
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAMTM, parametros: " + Arrays.toString(paramsAAMTM));
			try {
				resultado = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAMTM), paramsAAMTM);
			} catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20019, "NO EXISTE {}", "CAUSAL RELACIONADO A LA TRANSACCION");
			}
			
			if(UtileriaDeDatos.isNull(resultado)) {
				throw new ServicioException(20019, "NO EXISTE {}", "CAUSAL RELACIONADO A LA TRANSACCION");
			}	
			
		} catch (TipoDatoException e) {
			logger.error("Error recuperando objetos {}", e.getMessage(), e);
			throw new ServicioException(20010, "Error recuperando objetos auxiliares");
		}

	}
	
	/**
	 * M&eacutetodo para generar correlativo a partir de registro en tabla SFBDB_AAMTC
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionCorrelativo(DatosOperacion datos) throws ServicioException {

		try {
			Integer folio = 0;

			Object paramsAAMTC[] = { 
					new Integer(80) 
			};

			logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAMTC, parametros: " + Arrays.toString(paramsAAMTC));
			try {
				folio = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMTC), Integer.class, paramsAAMTC);
			} catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20551, "NO EXISTE CORRELATIVO - {}", "");
			}

			Integer folioAux = folio + 1;
			Object paramsAAMTC_UPDATE[] = { 
					folioAux, new Integer(80)
			};

			logger.debug("Ejecutando sentencia UPDATE LINC SFBDB AAMTC, parametros: " + Arrays.toString(paramsAAMTC_UPDATE));
			ejecutarSentencia(query(UPDATE_SFBDB_AAMTC), paramsAAMTC_UPDATE);

			datos.agregarDato("inuRegis", folioAux);

		} catch (ServicioException e) {
			logger.error("Error al recuperar correlativo:", e.getMessage(), e);
			throw new ServicioException(20551, "NO EXISTE CORRELATIVO - {}", "");
		}
	}
	
	/**
	 * Metodo para registrar la transaccion en el tanque de transacciones
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void registroTransaccion (DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		PagoMhPeticion peticion = datos.obtenerObjeto("peticion", PagoMhPeticion.class);
		
		Integer numTran =  jdbcTemplate.queryForObject(query(SELECT_FNC_CORREL_CANAL), Integer.class, Constantes.VENTANILLA);
		logger.debug("Ejecutando sentencia SELECT_FNC_CORREL_CANAL, parametros: " + numTran);
		try {
			datos.agregarDato("codCausal", peticion.getCodCausal());
			datos.agregarDato("codConcepto", Constantes.CONCEPTO_VE);
			datos.agregarDato("codOficina", 0);
			datos.agregarDato("codProducto", 0);
			datos.agregarDato("numCuenta", 0);
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			datos.agregarDato("fechaRelativa", datos.obtenerInteger("fechaRelativa"));
			datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("numDocumentoTran", datos.obtenerInteger("tnudoctra"));
			//codCompania viene de seguridad terminales financieros
			datos.agregarDato("codMoneda", Constantes.DOLAR);
			datos.agregarDato("digitoVerificador",0);
			datos.agregarDato("numCaja",peticion.getNumCaja());
			datos.agregarDato("montoIVA", null);
			datos.agregarDato("codTran",peticion.getCodTran());
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codDebCre", Constantes.CREDITO);
			datos.agregarDato("numSecuenciaCupon", 0);
			datos.agregarDato("valorImpuestoVenta", BigDecimal.ZERO);
			datos.agregarDato("codSectorEconomico", 0);
			//numDiasAtras null
			datos.agregarDato("fechaReal", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numDocumentoReversa", new Integer(0));
			//saldoAnterior va con null
			datos.agregarDato("senAJATR", Constantes.NO); 
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
			datos.agregarDato("senWANG", new Integer(0));
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.SI);
			datos.agregarDato("senPosteo", Constantes.NO);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", new BigDecimal(1));
			datos.agregarDato("valorMovimiento", datos.obtenerBigDecimal("montoPagar"));
			datos.agregarDato("valorCheque", new BigDecimal(0));
			datos.agregarDato("valorVenta", new BigDecimal(1));
			datos.agregarDato("numDocumentoTran2", new Integer(0));
			// valorChequesAjenos null
			// valorChequesExt null
			// valorChequesPropios null
			// descripcionTran va con null
			// codBancoTransf va con null
			datos.agregarDato("numCuentaTransf", "0000000000000");
			// codPaisTransf va con null
			datos.agregarDato("senACRM", Constantes.NO);
			// codCliente va con null
			// valorImpuesto null
			// tipDocumentoCliente null
			// numDocumentoCliente null
			// numDocumentoImp null
			datos.agregarDato("codSubCausal", peticion.getCodSubCausal());
					
			registrarTransaccionAAATR(datos);
		
	}catch(TipoDatoException e) {
		logger.error("Error en preparacion de transaccion para registrar en AAATR : " + e.getMessage(), e);
		throw new ServicioException(20001, "Error inesperado : " + e.getMessage());
		}
	}
	
	/**
	 * M&eacutetodo para insertar registro de transacción en tabla de control SFBDB_IEACH
	 * @param  datos
	 * @throws ServicioException
	 */
	private void insertarRegistroPagoTransacc(DatosOperacion datos) throws ServicioException {

		try {

	        
			PagoMhPeticion peticion = datos.obtenerObjeto("peticion", PagoMhPeticion.class);
			
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");			
			Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);			
			Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
			
			Long glbDtime = 0l; 

			//Obteniendo valor de glbDtime
			logger.debug("Ejecutando sentencia obtenerGlbDtime ");			
			if(UtileriaDeDatos.isEquals(glbDtime.intValue(), 0)) {
				glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME_DIF), Long.class);
				logger.debug("Ejecutando sentencia SELECT_GLBDTIME_DIF, parametros: " + glbDtime);
			}

			Object[] paramsIEACH= {
				 glbDtime
				,datos.obtenerString("desgCodBarra").trim()
				,datos.obtenerString("desgNpe").trim()
				,datos.obtenerInteger("inuRegis")
				,peticion.getCodOficinaTran()
				,peticion.getCodTerminal()
				,peticion.getCodCajero()
				,"ZE" //"AN" //ESTADO
				,new Integer(0)
				,new Integer(0)
				,fechaSistemaAMD
				,datos.obtenerInteger("fechaVencimiento")
				,new Integer(0)
				,datos.obtenerInteger("horaSistema") 
				,datos.obtenerBigDecimal("montoPagar")
				,datos.obtenerInteger("tnudoctra")
				," "
				,new Integer(0)			
				
			};

			logger.debug("Ejecutando sentencia INSERT_SFBDB_IEACH, parametros: " + Arrays.toString(paramsIEACH));
			ejecutarSentencia(query(INSERT_SFBDB_IEACH), paramsIEACH);	

		} catch (TipoDatoException | ParseException  e) {
			logger.error("Error inesperado:" + e.getMessage(), e);
			throw new ServicioException(20001, "Error inesperado: " + e.getMessage());
		}
	}
	
	@Deprecated
	private Integer calcularDigito(String codigo) {
//		Integer digito = Integer.parseInt(codigo.substring(codigo.length()-1));
		Integer digCalculado;
		Integer suma = 0, val1 = 0, val2 = 0;
		String codOrigen = codigo.substring(0, codigo.length()-1);
		
		for(int i = 0 ; i <= codOrigen.length() - 1 ; i+=2) {
			val1 = Integer.parseInt(String.valueOf(codOrigen.charAt(i)));
			val2 = (i>0) ? val2 = Integer.parseInt(String.valueOf(codOrigen.charAt(i-1))) : 0;
			suma += (val1 * 2) + ( (val1 > 4) ? 1 : 0 ) + val2;
		}
		
		digCalculado = (10 - (suma%10)%10);
		return digCalculado;
	}
	/**
	 * Método para calcular dígito verificador
	 * @param codigo
	 * @return
	 * @throws ServicioException
	 */
	private Integer calcularDigitoVerif(String codigo) throws ServicioException {
		
		String npe = null;
		
		try {
			if (UtileriaDeDatos.isEquals(codigo.trim().length(), new Integer(54))) {
				npe = codigo.trim().substring(12, 4) + codigo.trim().substring(21, 10) + codigo.trim().substring(33, 8)
						+ "0" + codigo.trim().substring(45, 10);
			} else {
				if (UtileriaDeDatos.isEquals(codigo.trim().length(), new Integer(34))) {
					npe = codigo.trim().substring(0);
				}
			}

			int sumaImpares = 0, sumaPares = 0, digitoNpe = 0, productoImpares = 0;

			for (int i = 1; i < npe.length(); i++) {
				digitoNpe = Integer.parseInt(String.valueOf(npe.charAt(i - 1)));
				if (i % 2 == 0) {
					sumaPares = sumaPares + digitoNpe;
				} else {
					productoImpares = digitoNpe * 2;
					if (productoImpares >= 10) {
						productoImpares = productoImpares + 1;
					} else {
						productoImpares = productoImpares + 0;
					}
					sumaImpares = sumaImpares + productoImpares;
				}
				productoImpares = 0;				
			}

			int a = sumaImpares + sumaPares;
			int b = a / 10;
			int c = b * 10;
			int d = a - c;
			int e = 10 - d;
			int f = e / 10;
			int g = f * 10;
			int vr = e - g;

			return vr;
		} catch (Exception e) {
			logger.error("Error inesperado en calcularDigitoVerif(String):" + e.getMessage(), e);
			throw new ServicioException(20363, "DIGITO VERIFICADOR ERRONEO-{}", "AL CALCULAR");
		}		
	}

	/**
	 * Metodo para validar dígito verificador de npe 
	 * @param datos
	 */
	@Deprecated
	private Boolean ValidarDigito(String codigo) {
		codigo = "100014334407740";
		Boolean ok = false;
		Integer digito = Integer.parseInt(codigo.substring(codigo.length()-1));
		Integer digCalculado;
		Integer suma = 0, val1 = 0, val2 = 0;
		String codOrigen = codigo.substring(0, codigo.length()-1);
		
		for(int i = 0 ; i <= codOrigen.length() - 1 ; i+=2) {
			val1 = Integer.parseInt(String.valueOf(codOrigen.charAt(i)));
			val2 = (i>0) ? val2 = Integer.parseInt(String.valueOf(codOrigen.charAt(i-1))) : 0;
			suma += (val1 * 2) + ( (val1 > 4) ? 1 : 0 ) + val2;
		}
		
		digCalculado = (10 - (suma%10)%10);
		
		if(digito == digCalculado) {
			ok = true;
		}
		
		return ok;
	}
       
	
	/**
	 * Método que valida el digito verificar del código de comprobante enviado, sea código de barra o NPE
	 * @param codigo
	 * @return
	 * @throws ServicioException 
	 */
	private Boolean validarDigitoVerif(String codigo) throws ServicioException {

		String npe = null;
		try {
			if (UtileriaDeDatos.isEquals(codigo.trim().length(), new Integer(54))) {
				// npe = Mid(barra.text,12,4) + Mid(barra.text,21,10) + Mid(barra.text,33,8) +
				// "0" + Mid(barra.text,45,10)
				npe = codigo.trim().substring(12, 4) + codigo.trim().substring(21, 10) + codigo.trim().substring(33, 8)
						+ "0" + codigo.trim().substring(45, 10);
			} else {
				if (UtileriaDeDatos.isEquals(codigo.trim().length(), new Integer(34))) {
					// npe = Mid(trim(barra.text),1,33)
					npe = codigo.trim().substring(0);
				}
			}

			Boolean ok = false;
			// Integer digito = Integer.parseInt(codigo.substring(codigo.length()-1));
			Integer digito = Integer.parseInt(npe.substring(npe.length() - 1));

			int sumaImpares = 0, sumaPares = 0, digitoNpe = 0, productoImpares = 0;

			for (int i = 1; i < npe.length(); i++) {
				digitoNpe = Integer.parseInt(String.valueOf(npe.charAt(i - 1)));
				if (i % 2 == 0) {
					sumaPares = sumaPares + digitoNpe;
				} else {
					productoImpares = digitoNpe * 2;
					if (productoImpares >= 10) {
						productoImpares = productoImpares + 1;
					} else {
						productoImpares = productoImpares + 0;
					}
					sumaImpares = sumaImpares + productoImpares;
				}

				productoImpares = 0;

			}

			int a = sumaImpares + sumaPares;
			int b = a / 10;
			int c = b * 10;
			int d = a - c;
			int e = 10 - d;
			int f = e / 10;
			int g = f * 10;
			int vr = e - g;

			if (digito == vr) {
				ok = true;
			}

			return ok;
		} catch (Exception e) {
			logger.error("Error inesperado en validarDigitoVerif(String):" + e.getMessage(), e);
			throw new ServicioException(20001, "Error inesperado: " + e.getMessage());
		}
	}
   	
	/**
	 * Método para validar la respuesta obtenida del Monitor NPE, al enviar la solicitud de pago
	 * @param datos
	 * @return
	 * @throws ServicioException
	 */
    @Deprecated
	@Transactional("transactionManager")
	private Integer validacionRespuestaMonitor(PagoMhRespuesta pagoMhRespuesta) throws ServicioException {
		logger.debug("Validando respuesta de monitor por 10s ...");
		DefaultTransactionDefinition paramTransactionDefinition = new DefaultTransactionDefinition();
		TransactionStatus status = platformTransactionManager.getTransaction(paramTransactionDefinition);
		DatosOperacion datos= new DatosOperacion();
		Integer codErr= 0;
		try {

			Object[] paramsIEACH2 = { pagoMhRespuesta.getIdTransaccionMh() };
			Map<String, Object> resultado = null;
			Integer ico_resmh = null;
			String ide_resmh = null;
			String ico_estad = null;
			Integer senReenvio = new Integer(0);

			for (int i = 0; i < 10; i++) {

				logger.debug(Thread.currentThread().getName() + "  " + i);
				logger.debug("AJ317 iteracion en SELECT_SFBDB_IEACH  " + i);

				// thread to sleep for 1000 milliseconds
				Thread.sleep(1000);
				try {
					logger.debug("AJ317 Ejecutando sentencia SELECT LINC SFBDB IEACH2, parametros: " + pagoMhRespuesta.getIdTransaccionMh());
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEACH2, parametros: " + paramsIEACH2.toString());
					resultado = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEACH2), paramsIEACH2);
				} catch (EmptyResultDataAccessException ignored) {

				}
				// respuesta de WS
				if (!UtileriaDeDatos.isNull(resultado)) {
					AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(resultado);
					ico_resmh = adaptador.getInteger("ico_resmh");
					ide_resmh = adaptador.getString("ide_resmh");
					ico_estad = adaptador.getString("ico_estad"); // ESTADO SE, RE, AN, PA
					logger.debug("AJ317 Estado registro:" + ico_estad);
					if (UtileriaDeDatos.isEquals(ico_resmh, Constantes.SI)
							&& UtileriaDeDatos.isEquals(ico_estad, "RE")) {
						logger.debug("AJ317 Respuesta de exito recibida de WS MMHH:" + ide_resmh);
						datos.agregarDato("ico_resmh", ico_resmh);
						datos.agregarDato("ide_resmh", ide_resmh);
						break;
					} else {
						if (UtileriaDeDatos.isEquals(ico_resmh, new Integer(0))
								&& UtileriaDeDatos.isEquals(ico_estad, "RE")) {
							logger.error("AJ317 Ocurrio un error inesperado consulta respuesta WS MMHH:" + ide_resmh);
							datos.agregarDato("ico_resmh", ico_resmh);
							datos.agregarDato("ide_resmh", ide_resmh);
							codErr = new Integer(1);

							break;

						}
					}
				}

			}
			// para reenvio
			if (UtileriaDeDatos.isEquals(ico_resmh, new Integer(0)) && !UtileriaDeDatos.isEquals(ico_estad, "RE")) {
				logger.debug("AJ317 Envia registro a reenvio codigo,estado: " + ico_resmh + "," + ico_estad);
				Object paramsIEACH_UPDATE[] = { 
						"RR" // AN, en power los anula comunicacion monitor - bfa no disponible
						, pagoMhRespuesta.getIdTransaccionMh()
						};
				logger.debug("AJ317 Ejecutando sentencia UPDATE_SFBDB_IEACH, parametros: " + paramsIEACH_UPDATE.toString());
				ejecutarSentencia(query(UPDATE_SFBDB_IEACH), paramsIEACH_UPDATE);

				datos.agregarDato("ico_resmh", new Integer(1)); // si no esta el valor ico_resmh y monitor deja SE																
				datos.agregarDato("ide_resmh", " ");
				senReenvio = 1;
			}

//			PagoMhRespuesta respuesta = datos.obtenerObjeto("pagoMhRespuesta", PagoMhRespuesta.class);
//			respuesta.setCodigo(datos.obtenerInteger("ico_resmh"));
			if (UtileriaDeDatos.isEquals(datos.obtenerInteger("ico_resmh"), new Integer(1)) && UtileriaDeDatos.isEquals(senReenvio, new Integer(0))) {
//				respuesta.setDescripcion("EXITO");
				Integer fechaSistema = pagoMhRespuesta.getFechaSistema();
				Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
				Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
				Object paramsIEACH_UPDATE2[] = { 
						"PA" //
						, fechaSistemaAMD
						, pagoMhRespuesta.getHoraSistema()
						, ide_resmh // EL MONITOR LO ACTUALIZA ICO_ESTAD, ICO_RESMH, IDE_RESMH, INU_TRAMH, WHERE INU_REGIS = ? ";
						, pagoMhRespuesta.getIdTransaccionMh() 
						
						};
				logger.debug("AJ317 Ejecutando sentencia UPDATE_SFBDB_IEACH2, parametros: " + paramsIEACH_UPDATE2.toString());
				ejecutarSentencia(query(UPDATE_SFBDB_IEACH2), paramsIEACH_UPDATE2);
			} else {
				if (UtileriaDeDatos.isEquals(senReenvio, Constantes.SI)) {
//					respuesta.setDescripcion("EXITO");
				} else {
//					respuesta.setDescripcion(datos.obtenerString("ide_resmh"));
				}
			}
			
			platformTransactionManager.commit(status);
//			return respuesta;
			return codErr;

		} catch (TipoDatoException | InterruptedException | ParseException e) {
			logger.error("Ocurrio un error inesperado al verificar pago:", e.getMessage(), e);
			platformTransactionManager.rollback(status);
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
