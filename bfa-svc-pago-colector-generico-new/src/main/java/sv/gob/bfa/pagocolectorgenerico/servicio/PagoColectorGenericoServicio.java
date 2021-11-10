package sv.gob.bfa.pagocolectorgenerico.servicio;

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
import sv.gob.bfa.pagocolectorgenerico.model.PagoColectorGenericoPeticion;
import sv.gob.bfa.pagocolectorgenerico.model.PagoColectorGenericoRespuesta;

public class PagoColectorGenericoServicio extends Servicio{
	
	private static final String NOM_COD_SERVICIO = "Colector Generico AJ499: ";
	
	private static final String SELECT_SFBDB_IEMCO = "SELECT IVA_MORA AS valorMora," + 
			"	IPO_MORA AS porcentajeMora," + 
			"	IDE_ALIAS AS aliasIEMCO," + 
			"	SCO_IDENT AS codClienteIEMCO " + 
			"	FROM LINC.SFBDB_IEMCO@DBLINK@ " + 
			"	WHERE ICO_COLECT = ?";
	
	private static final String SELECT_SFBDB_ICRDO = "SELECT ACO_CAUSA AS codCausal, " + 
			"	ACOSUBCAU AS codSubCausal, " + 
			"	ISEVALREG AS senValidacionRegistro, " + 
			"	ISEABOCTA AS senAbonoCuenta, " + 
			"	ACOTIPCOR AS codTipoCorrelativo, " + 
			"	ISEPAGEXT AS senPagoExtemporaneo, " + 
			"	IDEDOCUM AS descTipoDocumento " + 
			"	FROM LINC.SFBDB_ICRDO@DBLINK@ " + 
			"	WHERE ICO_COLECT = ? " + 
			"	AND ICOTIPDOC = ? ";
	
	private static final String SELECT_SFBDB_AAMTM = "SELECT ASESUBCAU AS senSubCausal " + 
			"	FROM LINC.SFBDB_AAMTM@DBLINK@ " + 
			" 	WHERE DCO_ISPEC = ? " +
			" 	AND ACO_CAUSA = ? ";
	
	private static final String SELECT_SFBDB_AAMSM= "SELECT ACU_PRODU AS codProductoCta," + 
			"	 ACU_OFICI AS codOficinaCta," + 
			" 	ACUNUMCUE AS numCuenta," + 
			" 	ACUDIGVER AS digitoVerificadorCta," + 
			" 	LPAD(ACU_PRODU, 3, 0) || LPAD(ACU_OFICI, 3, 0) || LPAD(ACUNUMCUE, 6, 0) || ACUDIGVER AS cuentaStr" + 
			" 	FROM   LINC.SFBDB_AAMSM@DBLINK@" + 
			"	 WHERE  DCO_ISPEC = ?" +
			"	 AND ACO_CAUSA = ?" +
			" 	AND ACOSUBCAU = ?";
	
	private static final String SELECT_SFBDB_IEAGE= "SELECT INO_CAMPO AS nombreCampo," + 
			"	 ISEVISIBLE AS senVisible," + 
			" 	INU_LARGO AS longitudCampo," + 
			" 	ISE_OBLIGA AS senObligatorio," + 
			" 	ISE_ARCHI AS senArchivo" + 
			" 	FROM LINC.SFBDB_IEAGE@DBLINK@" + 
			" 	WHERE ICO_COLECT = ?" +
			"	 AND INU_CAMPO > ?" + 
			"	 ORDER BY INU_CAMPO"
			;
	
	private static final String SELECT_SFBDB_ICATR = "SELECT INUREGCLI AS numRegistroCliente," + 
			"	TNUDOCTRA AS numDocumentoTran," + 
			"	IMO_PAGO AS montoPago" + 
			"	FROM LINC.SFBDB_ICATR@DBLINK@" + 
			"   WHERE IFE_TRANS = ?" + 
			"   AND DCO_OFICI = ?" + 
			"   AND DCO_TERMI = ?" + 
			"   AND DCO_USUAR = ?" + 
			"   AND INUREGCLI = ?" + 
			"   AND TNUDOCTRA = ?" + 
			"   AND IMO_PAGO = ?" + 
			"   AND INU_REGIS > ? " + 
			"   AND TSE_REVER != ?";
	
	private static final String SELECT_SFBDB_AAACM = "SELECT ADEREGCAR AS numRegArchivoCargado" + 
			"   FROM LINC.SFBDB_AAACM@DBLINK@" + 
			"   WHERE DCO_USUAR = ?" + 
			"   AND ANU_LOTE = ?" + 
			"   AND ACOESTREG = ?";
	
	private static final String SELECT_SFBDB_ICPGE = "SELECT INO_CLIEN AS nomCliente, " + 
			"	INUREGCLI AS numRegistroCliente, " + 
			"	ICO_ESTAD AS codEstado," + 
			"	IFE_PAGO AS fechaPago, " + 
			"	IVA_PAGO AS valorPago, " + 
			"	ICOTIPCLI AS tipoClienteICPGE," + 
			"	IFE_CARGA AS fechaCarga," + 
			"	IHO_CARGA AS horaCarga" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE ICO_COLECT = ?" + 
			"	 AND ICOTIPDOC = ?" + 
			"	 AND INUREGCLI = ?" + 
			"	 AND IFE_CARGA = ?";
	
	private static final String SELECT_SFBDB_ICPGE3 = "SELECT MAX(IFE_CARGA) FROM" + 
			"	LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE ICO_COLECT = ?" + 
			"	AND ICOTIPDOC = ?" + 
			"	AND INUREGCLI = ?";
	
	private static final String SELECT_SFBDB_IEAPC = "SELECT IFE_VENCIM AS fechaVencimiento" + 
			"	FROM (" + 
			"	SELECT IFE_VENCIM" + 
			"	FROM LINC.SFBDB_IEAPC@DBLINK@" + 
			"	WHERE ICO_COLECT = ?" + 
			"	 AND ICO_TIPDOC = ?" + 
			"	 AND IFE_VENCIM >= ?" + 
			"	 ORDER BY IFE_VENCIM DESC" + 
			"	) WHERE ROWNUM <= 1";
	
	private static final String SELECT_SFBDB_IEAPC2 = "SELECT IFE_VENCIM AS fechaVencimiento" + 
			"    FROM (" + 
			"    SELECT IFE_VENCIM" + 
			"     FROM LINC.SFBDB_IEAPC@DBLINK@" + 
			"    WHERE ICO_COLECT = ?" + 
			"     AND ICO_TIPDOC = ?" + 
			"     AND IFE_VENCIM >= ?" + 
			"     ORDER BY IFE_VENCIM DESC" + 
			"    ) WHERE ROWNUM <= 1";
	
	private static final String SELECT_SFBDB_ICPGE2 = "SELECT ICO_ESTAD AS codEstado, " + 
			"	IFE_PAGO AS fechaPago," + 
			"	IVA_PAGO AS valorPago," + 
			"	IFE_CARGA AS fechaCarga, " + 
			"	IHO_CARGA AS horaCarga" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE ICO_COLECT =?" + 
			"	AND ICOTIPDOC = ?" + 
			"	AND INUREGCLI = ?";
	
	private static final String SELECT_SFBDB_AAMPR = "SELECT ACO_CONCE AS codConceptoProducto" + 
			"	FROM   LINC.SFBDB_AAMPR@DBLINK@" + 
			"	WHERE  ACO_PRODU = ?";
	
	private static final String SELECT_SFBDB_AAMTC = "SELECT 1 AS senEncontrado," + 
			"	ANU_FOLIO AS numDocum" + 
			"	FROM LINC.SFBDB_AAMTC@DBLINK@" + 
			"	 WHERE ACOTIPCOR = ?" + 
			"	AND ACO_OFICI = ?" + 
			"	AND ACO_PRODU = ?";
	
	private static final String UPDATE_SFBDB_AAMTC = "UPDATE LINC.SFBDB_AAMTC@DBLINK@" + 
			"	SET ANU_FOLIO = ?" + 
			"	WHERE ACOTIPCOR = ?" + 
			"	AND ACO_OFICI = ?" + 
			"	AND ACO_PRODU = ?";
	
	private static final String UPDATE_SFBDB_ICPGE = "UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"    SET IFE_PAGO = ?," + 
			"    ICO_ESTAD = ? " + 
			"    WHERE ICO_COLECT = ?" + 
			"    AND ICOTIPDOC = ?" + 
			"    AND INUREGCLI = ?" + 
			"    AND IFE_CARGA = ?";
	
	private static final String SELECT_MADMIN_FNC_CORREL_CANAL = "SELECT MADMIN.FNC_CORREL_CANAL( ? ) as numTran FROM DUAL ";
	
	private final static String SELECT_GLBDTIME_DIF = "SELECT MADMIN.GENERATE_GLBDTIME_DIF as glbDtimeDAALA FROM DUAL";
	
	private static final String INSERT_SFBDB_ICATR = 
			"INSERT INTO LINC.SFBDB_ICATR@DBLINK@"+
			"(GLB_DTIME, ICO_COLECT," +
			"ICO_ESTAD, ICOTIPDOC," +
			"DCO_OFICI, DCO_TERMI," + 
			"DCO_USUAR,ICOTIPCLI," + 
			"IFE_TRANS,IHO_TRANS," + 
			"IMO_PAGO,INO_CLIEN," + 
			"INUIDECLI,IFE_CARGA," +
			"IHO_CARGA,INUREGCLI," + 
			"TNUDOCTRA,INU_REGIS," +
			"TSE_REVER,ICOTIPIDE," + 
			"IFE_VENCI,IPEDECLAR," + 
			"INUCTASFB,IMO_VALOR," +
			"TCO_CANAL,IFE_ENVIO," + 
			"IHO_ENVIO,THO_CARGA) "
			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
    private static String INSERT_SFBDB_P_ICATR = "INSERT INTO LINC.SFBDB_P_ICATR@DBLINK@" 
			  +" (IFE_TRANS, "
			  +" DCO_OFICI, "	  			  
			  +" DCO_TERMI, "
			  +" DCO_USUAR, "
			  +" INU_REGIS, "
			  +" GLB_DTIME "			  
			  + ") "					  
			  + " VALUES ("
			  +			" ?, ?, ?, "
			  +			" ?, ?, ? "
			  +			" ) ";
    
    private static String INSERT_SFBDB_P_ICATR01 = "INSERT INTO LINC.SFBDB_P_ICATR01@DBLINK@" 
			  +" (TCO_CANAL, "
    		  +" IFE_TRANS,"
			  +" ICO_COLECT, "	  			  
			  +" ICOTIPDOC, "	
			  +" INU_REGIS, "
			  +" GLB_DTIME "			  
			  + ") "					  
			  + " VALUES ("
			  +			" ?, ?, ?, "
			  +			" ?, ?, ? "
			  +			" ) ";
	
	Logger logger = LoggerFactory.getLogger(PagoColectorGenericoServicio.class);
	
	/**
	 * M&eacutetodo principal, contiene toda la logica del negocio
	 */
	@Override
	public Object procesar(Object objetoDom) throws ServicioException{
		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");
		
		logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> EntradasVariasPeticion");
		PagoColectorGenericoPeticion peticion = (PagoColectorGenericoPeticion) objetoDom;
		try {
			
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			validacionInicial(peticion);			
			
			datos.agregarDato("peticion",peticion);
			validacionParametro(datos);
//			datos.agregarPropiedadesDeObjeto(peticion);
			
			logger.debug(NOM_COD_SERVICIO + "Iniciando seguridad financiera de parametros...");
			seguridadTerminalFinancieros(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Definiendo variables que se usaran en el proceso");
			definirVariables(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando y validando datos de codigo de colector");
			recuperarValidarCodigoColector(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando y validando relacion colector con Documento de Pago");
			validarRelacionColectorConDocumentoDePago(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando relacion transacion Causal");
			validarTransacionCausal(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar relacion transaccion - subcausal");
			validarTransacionSubCausal(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar parametros Colector");
			obtenerParametrosColector(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Numero de documento sea distinto de cero");
			validarDocumentoDistintoCero(datos);
			
//			logger.debug(NOM_COD_SERVICIO + "Validar que valor del movimiento sea igual efectivo");
//			validarMovimientoIgualEfectivo(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar parametros del registro");
			validarParametrosDelRegistro(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Registrar AAATR");
			registarAAATR(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Verificar cuenta parametrizada y concepto para hacer nota de credito");
			verificarCuentaParametrizadaParaNotaCredito(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Actualizar registro en tabla ICPGE");
			ActualizarRegistroTablaICPGE(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Insertar registro en tabla SFBDB_ICATR");
			insertarRegistroTablaSFBDB_ICATR(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Asignación de variables de salida");
			AsignaciónVariablesSalida(datos);
			
			PagoColectorGenericoRespuesta respuesta = new PagoColectorGenericoRespuesta(); 
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			if(logger.isDebugEnabled()) {
				logger.debug(NOM_COD_SERVICIO + "RESPUESTA: {} ", respuesta);
			}
			
			return respuesta;
		
		} catch (ServicioException e) {
			logger.error("Ocurrio un error:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(e);
		} catch (TipoDatoException | ParseException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, NOM_COD_SERVICIO + "Error inesperado: " + e.getMessage()));
		} 
	}
	
	/**
	 * M&eacutetodo auxiliar definir las variables necesarias para el proceso de Servicio Colector Generico
	 * @param datos
	 * @throws TipoDatoException 
	 */
	
	private void definirVariables(DatosOperacion datos) throws TipoDatoException {
		
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);
		
		datos.agregarDato("codPantalla",Constantes.ISPEC_AJ499);
		datos.agregarDato("codConcepto",Constantes.CONCEPTO_VE);
		datos.agregarDato("longCampo", 0);
		datos.agregarDato("numCredito", "00000000000000000000");
		datos.agregarDato("senCampo1", 0);
		datos.agregarDato("senCampo5", 0);
		datos.agregarDato("senCampo6", 0);
		datos.agregarDato("codTipoIdentificacion", 0);
		datos.agregarDato("numIdentificacionCliente", "");
		datos.agregarDato("tipoCliente", "");
		datos.agregarDato("senPlanilla", Constantes.NO);
		datos.agregarDato("periodoDeclaracion", "");
		datos.agregarDato("valorMoraAux", BigDecimal.ZERO);
		datos.agregarDato("desMensaje", "");
		datos.agregarDato("codMoneda", 2);
		
		if(UtileriaDeDatos.isEquals(peticion.getCodTipoDocumento(), new Integer(0))) {
			peticion.setCodTipoDocumento(new Integer(1));
		}
		
		datos.agregarDato("peticion",peticion);
		datos.agregarPropiedadesDeObjeto(peticion);
		
	}

	/**
	 * M&eacutetodo auxiliar para validar peticion recibida
	 * @param peticion
	 * @throws ServicioException
	 */
	private void validacionInicial(PagoColectorGenericoPeticion peticion) throws ServicioException {
		logger.debug(NOM_COD_SERVICIO + "Iniciando validacion de parametros");
		logger.debug(NOM_COD_SERVICIO + "Peticion recibida: {}", peticion);
		
		UtileriaDeParametros.validarParametro(peticion.getCodColector(), "codColector", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTipoDocumento(), "codTipoDocumento", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getSenSupervisor(), "senSupervisor", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumLote(), "numLote", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);
		if (!UtileriaDeDatos.isNull(peticion.getValorEfectivo())) {
			UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		}
		if (!UtileriaDeDatos.isNull(peticion.getValorCheques())) {
			UtileriaDeParametros.validarParametro(peticion.getValorCheques(), "valorCheques", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para validar parametro recibido
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void validacionParametro(DatosOperacion datos) throws ServicioException, TipoDatoException {
		logger.debug(NOM_COD_SERVICIO + "Iniciando validacion de parametros");		
		
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);
		String numDocumentoTranAux = String.valueOf(peticion.getNumDocumentoTran());
		datos.agregarDato("digitoPex", new Integer(0));
		if(UtileriaDeDatos.isGreater(numDocumentoTranAux.length(), new Integer(8)) && UtileriaDeDatos.isEquals(peticion.getCodColector(), new Integer(148))) {
			peticion.setNumDocumentoTran(Integer.valueOf(numDocumentoTranAux.substring(0, 8)));		
			datos.agregarDato("digitoPex", Integer.valueOf(numDocumentoTranAux.substring(8)));
			datos.agregarDato("peticion",peticion);
		}
		datos.agregarPropiedadesDeObjeto(peticion);
	}
	
	/**
	 * M&eacutetodo auxiliar para invocar la función de soporte seguridad para terminales financieros 
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void seguridadTerminalFinancieros(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		logger.debug(NOM_COD_SERVICIO + "Invocando la funcion de soporte 'Seguridad para Terminales financieros' ...");
		seguridadTerminalesFinancieros(datos);
		
		Date fechaSistema = UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistema);
		
		datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);

	}
	
	/**
	 * M&eacutetodo auxiliar para recuperar y validar el Codigo de colector en Servicio Colector Generico
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void recuperarValidarCodigoColector(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);

		logger.debug("Ejecutando sentencia SELECT SFBDB IEMCO, parametro: " + peticion.getCodColector());
		try {
			Map<String, Object> nomCodColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEMCO), peticion.getCodColector());	
		
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(nomCodColector);
			String codClienteIEMCO = adaptador.getString("codClienteIEMCO");
			BigDecimal valorMora =  adaptador.getBigDecimal("valorMora");
			BigDecimal porcentajeMora = adaptador.getBigDecimal("porcentajeMora");
			String aliasIEMCO = adaptador.getString("aliasIEMCO");
			
			if(UtileriaDeDatos.isNull(codClienteIEMCO)) {
				throw new ServicioException(20019, "No existe {} ", "USUARIO IEMCO") ;
			}
			
			datos.agregarDato("codClienteIEMCO", codClienteIEMCO);
			datos.agregarDato("valorMora", valorMora);
			datos.agregarDato("porcentajeMora", porcentajeMora);
			datos.agregarDato("aliasIEMCO", aliasIEMCO);
			
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {} ", "COLECTOR DEFINIDO EN IEMCO") ;
		}
		
	}
	
	/**
	 * M&eacutetodo auxiliar para validar relacion colector con documento de pago
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	
	private void validarRelacionColectorConDocumentoDePago(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);

		logger.debug("Ejecutando sentencia SELECT SFBDB ICRDO, parametro: " + peticion.getCodColector() + "-"  + peticion.getCodTipoDocumento());
		try {
			Map<String, Object> nomCodColectorDocPago = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICRDO), peticion.getCodColector(),
																				peticion.getCodTipoDocumento());	
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(nomCodColectorDocPago);
			Integer codCausal = adaptador.getInteger("codCausal");
			Integer codSubCausal = adaptador.getInteger("codSubCausal");
			Integer codTipoCorrelativo = adaptador.getInteger("codTipoCorrelativo");
			Integer senValidacionRegistro = adaptador.getInteger("senValidacionRegistro");
			Integer senAbonoCuenta = adaptador.getInteger("senAbonoCuenta");
			Integer senPagoExtemporaneo = adaptador.getInteger("senPagoExtemporaneo");
			String descTipoDocumento = adaptador.getString("descTipoDocumento");
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("codSubCausal", codSubCausal);
			datos.agregarDato("codTipoCorrelativo", codTipoCorrelativo);
			datos.agregarDato("senValidacionRegistro", senValidacionRegistro);
			datos.agregarDato("senAbonoCuenta", senAbonoCuenta);
			datos.agregarDato("senPagoExtemporaneo", senPagoExtemporaneo);
			datos.agregarDato("descTipoDocumento", descTipoDocumento);
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {} ", "RELACION TIPO DE DOCUMENTO CON COLECTOR") ;
		}
		
	}
	
	/**
	 * M&eacutetodo auxiliar para recuperar y validar la relacion Transacion-causal
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarTransacionCausal(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		logger.debug("Ejecutando sentencia SELECT SFBDB AAMTM, parametro: " + datos.obtenerString("codPantalla") +"-" + datos.obtenerValor("codCausal"));
		try {
			Integer senSubCausal = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMTM), Integer.class,datos.obtenerString("codPantalla"),
																									datos.obtenerInteger("codCausal"));	
		
			if(!UtileriaDeDatos.isEquals(senSubCausal, Constantes.SI)) {
				throw new ServicioException(20005, "Senial incorrecta de uso subcausal en AAMTM");
			}
			
			datos.agregarDato("senSubCausal", senSubCausal);
			
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {} ", "CÓDIGO DE CAUSAL/TRANSACCIÓN");
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para recuperar y validar la relacion Transacion - subcausal
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarTransacionSubCausal(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		logger.debug("Ejecutando sentencia SELECT SFBDB AAMSM , parametro: " + datos.obtenerValor("codPantalla") + "-" + 
																			datos.obtenerValor("codCausal") + "-" + datos.obtenerValor("codSubCausal"));
		try {
			Map<String, Object> recTransAnual = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAMSM), datos.obtenerValor("codPantalla"), 
																		datos.obtenerValor("codCausal") , datos.obtenerValor("codSubCausal"));	
		
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(recTransAnual);
			Integer codProductoCta = adaptador.getInteger("codProductoCta");
			Integer codOficinaCta = adaptador.getInteger("codOficinaCta");
			Integer numCuenta = adaptador.getInteger("numCuenta");
			Integer digitoVerificadorCta = adaptador.getInteger("digitoVerificadorCta");
			datos.agregarDato("codProductoCta", codProductoCta);
			datos.agregarDato("numCuenta", numCuenta);
			datos.agregarDato("numCuentaAux", numCuenta);
			datos.agregarDato("digitoVerificadorCta", digitoVerificadorCta);
			Cliente cliente = new Cliente();
			String cuentaTransaccion = adaptador.getString("cuentaStr");
			datos.agregarDato("cuentaStr", cuentaTransaccion);
			Integer conceptoProducto = Integer.parseInt(codProductoCta.toString().substring(0,1));
			if(UtileriaDeDatos.isEquals(conceptoProducto, Constantes.CONCEPTO_CC)) {
				CuentaCorriente recuperarDatosCuentaCorriente = recuperarDatosCuentaCorriente(cuentaTransaccion);
				cliente = recuperarDatosCliente(recuperarDatosCuentaCorriente.getCodCliente());
				datos.agregarDato("cliente",cliente);
			}
			if(UtileriaDeDatos.isEquals(conceptoProducto, Constantes.CONCEPTO_AH)) {
				CuentaAhorro recuperarDatosCuentaAhorro = recuperarDatosCuentaAhorro(cuentaTransaccion);
				cliente = recuperarDatosCliente(recuperarDatosCuentaAhorro.getCodCliente());
				datos.agregarDato("cliente",cliente);
			}
			datos.agregarDato("codSectorEconomicoCliente",cliente.getCodSectorEconomicoCliente());
			datos.agregarDato("codCliente",cliente.getCodCliente());
			datos.agregarDato("tipDocumentoCliente",cliente.getTipDocumentoCliente());
			datos.agregarDato("numDocumentoCliente",cliente.getNumDocumentoCliente());
			datos.agregarDato("cliente", cliente);
			datos.agregarDato("codOficinaCta", codOficinaCta);
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {} ", "CODIGO SUBCASUAL/TRANSACCION");
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para obtener parametros del colector
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void obtenerParametrosColector(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);
		List<Map<String, Object>>  listadoCamposColector = null;
		try {
			
			Object[] paramsIEAGE = {
					peticion.getCodColector(),
					new Integer(0)
			};
			
			logger.debug("Ejecutando sentencia SELECT SFBDB IEAGE, parametros: {}", paramsIEAGE);
			listadoCamposColector = jdbcTemplate.queryForList(query(SELECT_SFBDB_IEAGE), paramsIEAGE );
			
			if(UtileriaDeDatos.listIsEmptyOrNull(listadoCamposColector)) {
				throw new ServicioException(20019, "No existe parametros de colector.", "PARAMETROS DE COLECTOR");
			}
			
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {} ", " PARAMETROS DE COLECTOR");
		}
			
			AdaptadorDeMapa adaptador;
			Integer posicionAnterior = 0;
			boolean fechaValida = Boolean.FALSE;
			Integer longCampo = 0;	
			String numCredito = "";
			String numIdentificacionCliente = "";
			Integer senCampo1				= 0;
			Integer senCampo3				= 0;
			Integer senCampo5				= 0;
			Integer senCampo6				= 0;
			
			for (Map<String, Object> map : listadoCamposColector) {
				adaptador = UtileriaDeDatos.adaptarMapa(map);
				String nombreCampo = adaptador.getString("nombreCampo");
				Integer senVisible = adaptador.getInteger("senVisible");
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "NOCREDITO") && 
					UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
						longCampo = adaptador.getInteger("longitudCampo");
						numCredito = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						numCredito = StringUtils.leftPad(numCredito, 20, '0');
						datos.agregarDato("numCredito", numCredito);
						if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
							senCampo1 = Constantes.SI;
						}
						posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "ICOTIPIDE") && 
					UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
						longCampo = adaptador.getInteger("longitudCampo");
						datos.agregarDato("codTipoIdentificacion", Integer.parseInt(peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo)));
						posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "INUIDECLI") &&
						UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
						longCampo = adaptador.getInteger("longitudCampo");
						numIdentificacionCliente = peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo);
						numIdentificacionCliente = StringUtils.leftPad(numIdentificacionCliente, 14, '0');
						datos.agregarDato("numIdentificacionCliente", numIdentificacionCliente);
						if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
							senCampo3 = Constantes.SI;
						}
						posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "ICOTIPCLI") && 
						UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
						longCampo = adaptador.getInteger("longitudCampo");
						datos.agregarDato("tipoCliente", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
						posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "IPE-DECLAR") && 
					UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
						longCampo = adaptador.getInteger("longitudCampo");
						datos.agregarDato("periodoDeclaracion", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
						if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
							senCampo5 = Constantes.SI;
						}
						posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "IVALMORA") && 
					UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
						longCampo = adaptador.getInteger("longitudCampo");
						datos.agregarDato("valorMoraAux", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
						posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "CARGA") && 
					UtileriaDeDatos.isEquals(adaptador.getInteger("senArchivo"), Constantes.SI)) {
						datos.agregarDato("senPlanilla", adaptador.getInteger("senArchivo"));
						posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "INO-CLIEN") && 
					UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
						longCampo = adaptador.getInteger("longitudCampo");
						datos.agregarDato("nomCliente", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
						if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
							senCampo6 = Constantes.SI;
						}
						posicionAnterior += longCampo;
				}
				datos.agregarDato("nommbreCampo", nombreCampo);
				datos.agregarDato("senVisible", senVisible);
				datos.agregarDato("longCampo", longCampo);
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "MMAAXX")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"MMAA");
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "DDMMAA")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"DDMMAA");
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "AAMMDD")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"AAMMDD");
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "MMAAAA")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"MMAAAA");
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "AAAAMM")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"AAAAMM");
				}
			}
			if(UtileriaDeDatos.isEquals(Constantes.SI, senCampo1) && 
					UtileriaDeDatos.isEquals(StringUtils.leftPad("", 20, "0"), datos.obtenerString("numCredito"))) {
				throw new ServicioException(20589, "Número incorrecto – Numero Credito invalido");
			}
			if(UtileriaDeDatos.isEquals(Constantes.SI, senCampo3) && 
					UtileriaDeDatos.isEquals("0", datos.obtenerString("numIdentificacionCliente"))){
				throw new ServicioException(20589, "Número incorrecto – Numero Identificacion invalido");
			}
			if(!fechaValida && UtileriaDeDatos.isEquals(Constantes.SI, senCampo5)) {
				throw new ServicioException(20003, "Fecha incorrecta {}", " PERIODO NO VALIDO");
			}
			if(UtileriaDeDatos.isEquals(Constantes.SI, senCampo6) && 
					UtileriaDeDatos.isEquals(0, datos.obtenerString("nomCliente").length())) {
				throw new ServicioException(20569, "Ingrese nombre del cliente");
			}
	}
	
	/**
	 * M&eacutetodo auxiliar para asegurarse que el numero de documento es diferente de cero
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarDocumentoDistintoCero(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		Integer codTipoCorrelativo = datos.obtenerInteger("codTipoCorrelativo");
		Integer numDocumentoTran = datos.obtenerInteger("numDocumentoTran");
		
		if(UtileriaDeDatos.isEquals(new Integer(0), codTipoCorrelativo) && UtileriaDeDatos.isEquals(new Integer(0), numDocumentoTran)) {
			throw new ServicioException(20004, "Documento Incorrecto") ;
		}
		
		validarMovimientoIgualEfectivo(datos);
		
	}
	
	/**
	 * M&eacutetodo auxiliar para asegurarse que el valor de movimiento es igual a efectivo
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarMovimientoIgualEfectivo(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);
		
		//Se valida movimiento cuadrado solo si se tienen valores efectivo o cheque
		if (
			(!UtileriaDeDatos.isNull(peticion.getValorEfectivo()) && UtileriaDeDatos.isGreater(peticion.getValorEfectivo(), BigDecimal.ZERO)) || 
			(!UtileriaDeDatos.isNull(peticion.getValorCheques()) && UtileriaDeDatos.isGreater(peticion.getValorCheques(), BigDecimal.ZERO))
			) {

			if (UtileriaDeDatos.isNull(peticion.getValorEfectivo())) peticion.setValorEfectivo(BigDecimal.ZERO);
			if (UtileriaDeDatos.isNull(peticion.getValorCheques())) peticion.setValorCheques(BigDecimal.ZERO);
			
			BigDecimal fondos = peticion.getValorEfectivo().add(peticion.getValorCheques());
			
			if (!UtileriaDeDatos.isEquals(fondos, peticion.getValorMovimiento())) {
				throw new ServicioException(20286, "Valor del movimiento no está cuadrado");
			}
			
		}
		
	}
	
	/**
	 * M&eacutetodo auxiliar para validar parametros del registro
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarParametrosDelRegistro(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);
		
		String nomCliente = "";
		String numRegistroCliente = "";
		String codEstado = "";
		Integer fechaPago = null;
		BigDecimal valorPago = null;
		Integer tipoClienteICPGE = null;
		Integer fechaCarga = null;
		Integer horaCarga = null;
		Integer fechaVencimiento = 0;
		String impresionFISDL = "";
		
		Integer fechaUltimaHabilAMD = 0;
		recuperarUltimoDiaHabilMes(datos);
		fechaUltimaHabilAMD = datos.obtenerInteger("fechaUltimaHabilMes");
        
        AdaptadorDeMapa	adaptador = null;
		String codColectorAux = "";
		String numCreditoAux = "";
		BigDecimal valorPagoAux = BigDecimal.ZERO;
		BigDecimal totalPlan = BigDecimal.ZERO;
		BigDecimal valorMoraAux = datos.obtenerBigDecimal("valorMoraAux");
		BigDecimal montoAux = BigDecimal.ZERO;
		BigDecimal porcentaje = BigDecimal.ZERO;
		BigDecimal moraCalculada = BigDecimal.ZERO;
        
		if(UtileriaDeDatos.isGreater(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_DIFERENTE_CUOTA) || 
			UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), new Integer(0))) {
			throw new ServicioException(20018, "VALOR INCORRECTO {}", "PARAMETRO DE VALIDACION DE REGISTRO");
		}
		Integer senPlanilla = datos.obtenerInteger("senPlanilla");
		if(UtileriaDeDatos.isEquals(senPlanilla, Constantes.NO)) {
			logger.debug("Iniciando condicion senPlantilla = NO");
			
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA)  || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_DIFERENTE_CUOTA)) {
				try {
					
					Object[] params_ICPGE2 = {
							datos.obtenerInteger("codColector"), 
							datos.obtenerInteger("codTipoDocumento"),
							StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
					};
					
					Integer IFE_CARGA = 0;
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE2, parametros: {}", Arrays.toString(params_ICPGE2));
					IFE_CARGA = jdbcTemplate.queryForObject(query(SELECT_SFBDB_ICPGE3), Integer.class, params_ICPGE2);
					
					Object[] params_ICPGE = {
							datos.obtenerInteger("codColector"), 
							datos.obtenerInteger("codTipoDocumento"),
							StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
							IFE_CARGA,
					};
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}", Arrays.toString(params_ICPGE));
					Map<String, Object> regCuotaColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICPGE), params_ICPGE);	
					adaptador = UtileriaDeDatos.adaptarMapa(regCuotaColector);
					
					nomCliente = adaptador.getString("nomCliente");
					numRegistroCliente = adaptador.getString("numRegistroCliente");
					codEstado =  adaptador.getString("codEstado");
					fechaPago =  adaptador.getInteger("fechaPago");
					valorPago =  adaptador.getBigDecimal("valorPago");
					tipoClienteICPGE =  adaptador.getInteger("tipoClienteICPGE");
					fechaCarga =  adaptador.getInteger("fechaCarga");
					horaCarga = adaptador.getInteger("horaCarga");
					
					datos.agregarDato("fechaCarga", fechaCarga);
					datos.agregarDato("horaCarga", horaCarga);
					datos.agregarDato("nomCliente", nomCliente);
					
				}catch (EmptyResultDataAccessException e) {
					throw new ServicioException(20019, "No existe {} ", " REGISTRO DE CUOTA BASE-COLECTOR");
				}
			}
			
			
			
			if((UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA))
					&& 
				(!UtileriaDeDatos.isEquals(fechaPago, new Integer(0)) || 
				 !UtileriaDeDatos.isEquals(codEstado, "I"))) {
				throw new ServicioException(20020, "Ya existe {}", "CUOTA PAGADA EN BASE DE PAGOS");
			}
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) && 
			  !UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), valorPago)) {
				throw new ServicioException(20018, "VALOR INCORRECTO {} ","DIFIERE DEL VALOR CUOTA A PAGAR");
			}
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA) && 
				UtileriaDeDatos.lessThan(peticion.getValorMovimiento(), valorPago)){
				throw new ServicioException(20018, "VALOR INCORRECTO {} ","ES MENOR AL VALOR CUOTA A PAGAR");
			}
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_PAGO) && 
				UtileriaDeDatos.isGreater(valorMoraAux, BigDecimal.ZERO)) {
				throw new ServicioException(20019, "No existe {} ", "NO DEBE INGRESAR MORA");
			}
			if(UtileriaDeDatos.isGreater(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), new Integer(0))) {
				throw new ServicioException(20018, "VALOR INCORRECTO {}","PARAMETRO PAGO EXTEMPORANEO");
			}
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)){
				try {
					
					Object[] params_IEAPC = {
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							fechaUltimaHabilAMD,
							new Integer(1)
					};
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
					fechaVencimiento = jdbcTemplate.queryForObject(query(SELECT_SFBDB_IEAPC), Integer.class, params_IEAPC);
				}catch (EmptyResultDataAccessException e) {
					try {
						
						Object[] params_IEAPC = {
								peticion.getCodColector(),
								Constantes.GC_CODIGO_TIPO_DOCUMENTO,
								fechaUltimaHabilAMD,
								new Integer(1)
						};
						
						logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
						fechaVencimiento = jdbcTemplate.queryForObject(query(SELECT_SFBDB_IEAPC2), Integer.class, params_IEAPC);
					}catch (EmptyResultDataAccessException e2) {
						throw new ServicioException(20019, "No existe {} ", "TIPO DOCUMENTO CON PARAMETRO VALIDACION DE FECHA");
					}
				}
				if(UtileriaDeDatos.isGreater(valorMoraAux, BigDecimal.ZERO) && 
				  !UtileriaDeDatos.isEquals(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) && 
				   UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {
					throw new ServicioException(20018, "VALOR INCORRECTO {}","MORA NO ES REQUERIDA");
				}
				if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"),fechaVencimiento) && 
					UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)) {
					throw new ServicioException(20003, "Fecha incorrecta {}", " VENCIDA PARA REALIZAR PAGO");
				}
				if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) && 
					UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {
					
					if(UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO) && 
						UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("valorMora"), BigDecimal.ZERO)	) {
						throw new ServicioException(20019, "No existe {} ", "PARAMETRO POR COBRAR MORA");
					}
					montoAux = peticion.getValorMovimiento();
					montoAux = montoAux.subtract(datos.obtenerBigDecimal("valorMora"));
					
					if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO)) {
						porcentaje = datos.obtenerBigDecimal("porcentajeMora");
						porcentaje = porcentaje.divide(new BigDecimal(100));
						
						moraCalculada = montoAux;
						moraCalculada = moraCalculada.multiply(porcentaje);
						moraCalculada = montoAux.multiply(porcentaje);
						moraCalculada = moraCalculada.setScale(2, BigDecimal.ROUND_HALF_UP);
						
						if(!(UtileriaDeDatos.isEquals(valorMoraAux, moraCalculada))) {
							throw new ServicioException(20018, "VALOR INCORRECTO {}","RECARGO POR MORA %");
						}
						if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("valorMora"), new BigDecimal(0)) && 
						  !UtileriaDeDatos.isEquals(valorMoraAux, datos.obtenerBigDecimal("valorMora"))) {
							throw new ServicioException(20018, "VALOR INCORRECTO {}","DE RECARGO POR MORA");
						}
					}
				}
			}
			
			Object[] paramsICATR = {
				datos.obtenerInteger("fechaSistemaAMD"),
//				datos.obtenerInteger("codOficinaCta"),
				peticion.getCodOficinaTran(),
				peticion.getCodTerminal(),
				peticion.getCodCajero(),
				datos.obtenerString("numCredito"),
				peticion.getNumDocumentoTran(), 
				peticion.getValorMovimiento(),
				new Integer(0),
				Constantes.SI
			};
			logger.debug("Ejecutando sentencia SELECT SFBDB ICATR, parametro: {}", Arrays.toString(paramsICATR));
			
			try {
				logger.debug("Ejecutando sentencia SELECT SFBDB ICATR, parametro: {}", Arrays.toString(paramsICATR));
				List<Map<String, Object>> transaccionesColector = jdbcTemplate.queryForList(query(SELECT_SFBDB_ICATR),paramsICATR);

				if(!UtileriaDeDatos.listIsEmptyOrNull(transaccionesColector)) {
					throw new ServicioException(20020, "Ya existe {}", "TRANSACCION DE CUOTA PAGADA");
				}
			}catch (EmptyResultDataAccessException ignored) {
			}
			
			
			
		}
		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"), Constantes.SI)) {
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) || 
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA)  || 
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_DIFERENTE_CUOTA)) {
				try {
					
					Object[] params_AAACM = {
							datos.obtenerString("codCajero"), 
							datos.obtenerInteger("numLote") , 
							Constantes.GC_ESTADO_COLECTOR_PENDIENTE
					};
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAACM, parametros: {}", Arrays.toString(params_AAACM));
					List<Map<String, Object>> listadoCargasArchivo = jdbcTemplate.queryForList(query(SELECT_SFBDB_AAACM), params_AAACM);	
					for(Map<String, Object> map : listadoCargasArchivo) {
						adaptador = UtileriaDeDatos.adaptarMapa(map);
						String numRegArchivoCargado = adaptador.getString("numRegArchivoCargado");
						try {
							
							Object[] params_ICPGE = {
									datos.obtenerInteger("codColector"), 
									datos.obtenerInteger("codTipoDocumento"),
									datos.obtenerString("numCredito")
							};
							
							logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}", Arrays.toString(params_ICPGE));
							Map<String, Object> registroTransaccionColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICPGE2), params_ICPGE);
							adaptador = UtileriaDeDatos.adaptarMapa(registroTransaccionColector);
							codEstado = adaptador.getString("codEstado");
							fechaPago = adaptador.getInteger("fechaPago");
							valorPago = adaptador.getBigDecimal("valorPago");
							valorPagoAux = UtileriaDeDatos.formatBigDecimal(numRegArchivoCargado.substring(57, 69));
							
							totalPlan = totalPlan.add((valorPagoAux));
							
							if((UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) || 
								UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA)) 
									&& 
								(!(UtileriaDeDatos.isEquals(codEstado, "I")) || !(UtileriaDeDatos.isEquals(fechaPago, new Integer(0))))){
								throw new ServicioException(20020, "YA EXISTE {}", "CUOTA PAGADA EN BASE DE PAGOS");
							}
							if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) && 
									!(UtileriaDeDatos.isEquals(valorPagoAux, valorPago))) {
								throw new ServicioException(20018, "VALOR INCORRECTO {}","DIFIERE DEL VALOR CUOTA A PAGAR");
							}
							if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA) && 
								UtileriaDeDatos.isGreater(valorPago, valorPagoAux)) {
								throw new ServicioException(20018, "VALOR INCORRECTO {}","DIFIERE DEL VALOR CUOTA A PAGAR");
							}
							if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_PAGO) && 
								UtileriaDeDatos.isGreater(valorMoraAux, BigDecimal.ZERO)) {
								throw new ServicioException(20019, "NO EXISTE {} ", " NO DEBE INGRESAR MORA");
							}
							if(UtileriaDeDatos.isGreater(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO) || 
								UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), new Integer(0))){
								throw new ServicioException(20018, "VALOR INCORRECTO {}", "PARAMETRO PAGO EXTEMPORANEO");
							}
							if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA) || 
								UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)){
								
								try {
									
									Object[] params_IEAPC = {
											datos.obtenerInteger("codColector") , 
											datos.obtenerInteger("codTipoDocumento") , 
											datos.obtenerInteger("fechaUltimaHabilAMD")
									};
									
									logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
									Map<String, Object> selectFechaVencimiento = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEAPC),params_IEAPC);	
									
									adaptador = UtileriaDeDatos.adaptarMapa(selectFechaVencimiento);
									fechaVencimiento = adaptador.getInteger("fechaVencimiento");
								}catch (EmptyResultDataAccessException e) {
									
									try {
										
										Object[] params_IEAPC = {
												datos.obtenerInteger("codColector") , 
												Constantes.GC_CODIGO_TIPO_DOCUMENTO , 
												datos.obtenerInteger("fechaUltimaHabilAMD")
										};
										
										logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
										Map<String, Object> selectFechaVencimiento = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEAPC2),params_IEAPC);	
										adaptador = UtileriaDeDatos.adaptarMapa(selectFechaVencimiento);
										fechaVencimiento = adaptador.getInteger("fechaVencimiento");
									}catch (EmptyResultDataAccessException e2) {
										throw new ServicioException(20019, "NO EXISTE {} ", "TIPO DOCUMENTO CON PARAMETRO VALIDACION DE FECHA");
									}
								}
								if(UtileriaDeDatos.isGreater(valorMoraAux, BigDecimal.ZERO) && 
									!(UtileriaDeDatos.isEquals(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento)) 
									&& 
									UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {
									throw new ServicioException(20018, "VALOR INCORRECTO {}","MORA NO ES REQUERIDA");
								}
								if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) && 
									UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)) {
									throw new ServicioException(20003, "Fecha incorrecta {}", " VENCIDA PARA REALIZAR PAGO");
								}
								if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) && 
									UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {
									
									if(UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO) && 
										UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("valorMora"), BigDecimal.ZERO)	) {
										throw new ServicioException(20019, "NO EXISTE {} ", "PARAMETRO POR COBRAR MORA");
									}
									 
									 montoAux = valorPagoAux;
									 montoAux = montoAux.subtract(datos.obtenerBigDecimal("valorMoraAux"));
									 
									if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO)) {
										porcentaje = datos.obtenerBigDecimal("porcentajeMora");
										porcentaje = porcentaje.divide(new BigDecimal(100));

										moraCalculada = montoAux;
										moraCalculada = moraCalculada.multiply(porcentaje);
										moraCalculada = moraCalculada.setScale(2, BigDecimal.ROUND_HALF_UP);
										
										if(!(UtileriaDeDatos.isEquals(valorMoraAux, moraCalculada))) {
											throw new ServicioException(20018, "VALOR INCORRECTO {}","RECARGO POR MORA %");
										}
										
										if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("valorMora"), BigDecimal.ZERO)) {
											if(!UtileriaDeDatos.isEquals(valorMoraAux, datos.obtenerBigDecimal("valorMora"))) {
													throw new ServicioException(20018, "VALOR INCORRECTO {}"," DE RECARGO POR MORA");
												}
										}
									}
								}
							}
						}catch (EmptyResultDataAccessException e) {
							throw new ServicioException(20019, "No existe {} ", "PAGO DOCUMENTO NO."
																+ datos.obtenerString("numCredito") + " EN ARCHIVO CARGADO.");
						}
						
						Object[] paramsICATR = {
							datos.obtenerInteger("fechaSistemaAMD"),
							peticion.getCodOficinaTran(),
							datos.obtenerInteger("codTerminalTran"),
							datos.obtenerString("codCajero"),
							datos.obtenerString("numCredito"),
							datos.obtenerInteger("numDocumentoTran"),
							datos.obtenerBigDecimal("valorMovimiento"),
							Constantes.SI
						};
						logger.debug("Ejecutando sentencia SELECT SFBDB ICATR, parametro: {}", Arrays.toString(paramsICATR));
						try {
							Map<String, Object> transaccionesColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICATR),paramsICATR);

							if(!UtileriaDeDatos.mapIsEmptyOrNull(transaccionesColector)) {
								throw new ServicioException(20020, "Ya existe transacción de cuota pagada");
							}
						}catch (EmptyResultDataAccessException ignored) {
						}
					}
				}catch (EmptyResultDataAccessException e) {
					throw new ServicioException(20019, "NO EXISTE {} ", "ARCHIVO CARGADO");
				}
			}

			if(!(UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), totalPlan))) {
				throw new ServicioException(20224, "VALOR DE LA TRANSACCIÓN INCORRECTO {} ", "C/TOTAL ARCH. CARGADO");
			}
			nomCliente = datos.obtenerString("aliasIEMCO");
			datos.agregarDato("nomCliente", nomCliente);
			numRegistroCliente = datos.obtenerString("codClienteIEMCO");
			datos.agregarDato("numRegistroCliente", numRegistroCliente);
		}
		
		datos.agregarDato("fechaVencimiento", fechaVencimiento);
		datos.agregarDato("tipoClienteICPGE", tipoClienteICPGE);
	}
	
	/**
	 * M&eacutetodo auxiliar para registrar en AAATR
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void registarAAATR(DatosOperacion datos) throws ServicioException, TipoDatoException {

		Integer numTran = 0;
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);
		
		logger.debug("Ejecutando sentencia SELECT MADMIN FNC CORREL CANAL, parametro:" + Constantes.VENTANILLA);
		try {
			Map<String, Object> numeroTransacion = jdbcTemplate.queryForMap(query(SELECT_MADMIN_FNC_CORREL_CANAL),Constantes.VENTANILLA);
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(numeroTransacion);
			numTran = adaptador.getInteger("numTran");
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe  ", "NUMERO DE TRANSACCION");
		}
		
		logger.debug("Preparando datos para registrar en tabla AAATR");
		//codCasual recuperada en  Validar relación colector con documento de pago
		datos.agregarDato("codConcepto", Constantes.CONCEPTO_VE);
		datos.agregarDato("codOficina", peticion.getCodOficinaTran());
		//codProductoCta recuperada en Validar relación transacción-subcausal
		datos.agregarDato("numCuenta", 0);
		//digitoVerificadorCta recuperada en Validar relación transacción-subcausal
		datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
		datos.agregarDato("codTerminal", peticion.getCodTerminal());
		//fecha relativa recuperada de Seguridad Terminales Financieros
		//hora sistema recuperada de Seguridad Terminales Financieros
		datos.agregarDato("numTran", numTran);
		datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
		//codCompania recuperada de Seguridad Terminales Financieros
		//codMoneda agregada en definicion de variables
		datos.agregarDato("numCaja", peticion.getNumCaja());
		datos.agregarDato("montoIva", null);
		datos.agregarDato("codTran", peticion.getCodTran());
		datos.agregarDato("codCajero", peticion.getCodCajero());
		datos.agregarDato("codDebCre", Constantes.CREDITO);
		datos.agregarDato("numSecuenciaCupon", null);
		datos.agregarDato("valorImpuestoVenta", null);
		//codSectorEconomicoCliente recuperada en Validar relación transacción-subcausal 
		datos.agregarDato("numDiasAtras", null);
		//fechaSistema recuperada de Seguridad Terminales Financieros
		//fechaTran recuperada de Seguridad Terminales Financieros
		datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
		datos.agregarDato("numDocumentoReversa", null);
		datos.agregarDato("numDocumentoReversa", null);
		datos.agregarDato("saldoAnterior", null);
		datos.agregarDato("senAJATR", Constantes.NO);
		datos.agregarDato("senAutorizacion", Constantes.NO);
		datos.agregarDato("senReversa", Constantes.NO);
		datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
		datos.agregarDato("senWANG", 0);
		datos.agregarDato("senDiaAnterior", Constantes.NO);
		datos.agregarDato("senImpCaja", Constantes.SI);
		datos.agregarDato("senPosteo", Constantes.NO);
		datos.agregarDato("valorAnterior", BigDecimal.ZERO);
		datos.agregarDato("valorCompra", BigDecimal.ONE);
		datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
		datos.agregarDato("valorCheque", peticion.getValorCheques());
		datos.agregarDato("valorVenta", BigDecimal.ONE);
		datos.agregarDato("numDocumentoTran2", UtileriaDeDatos.isEquals(peticion.getCodColector(),new Integer(148)) == true ? datos.obtenerInteger("digitoPex") : null);
		datos.agregarDato("valorChequesAjenos", peticion.getValorCheques());
		datos.agregarDato("valorChequesExt", null);
		datos.agregarDato("valorChequesPropios", null);
		datos.agregarDato("descripcionTran", " ");		
		datos.agregarDato("codBancoTransf", null);
		datos.agregarDato("numCuentaTransf", "0000000000000");
		datos.agregarDato("codPaisTransf", null);
		datos.agregarDato("senACRM", Constantes.SI);
		datos.agregarDato("codCliente", " ");
		datos.agregarDato("valorImpuesto", null);
		//tipDocumentoCliente recuperada en Validar relación transacción-subcausal 
		//numDocumentoCliente recuperada en Validar relación transacción-subcausal 
		datos.agregarDato("numDocumentoImp", null);
		//codSubCasual recuperada en  Validar relación colector con documento de pago
		registrarTransaccionAAATR(datos);
	}
	
	/**
	 * M&eacutetodo auxiliar Verificar cuenta parametrizada y concepto para hacer nota de credito
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void verificarCuentaParametrizadaParaNotaCredito(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		Integer senEncontrado = 0;
		Integer numDocum = 0;
		Integer codConceptoProducto = 0;
		String numCtaAAMSM = " ";
		if(UtileriaDeDatos.isGreater(datos.obtenerInteger("codProductoCta"), 0) && 
				UtileriaDeDatos.isGreater(datos.obtenerInteger("codOficinaCta"), 0) && 
				UtileriaDeDatos.isGreater(datos.obtenerInteger("numCuentaAux"), 0) && 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senAbonoCuenta"), Constantes.SI)) {
			numCtaAAMSM = StringUtils.leftPad(datos.obtenerInteger("codProductoCta").toString(), 3, '0')+
					StringUtils.leftPad(datos.obtenerInteger("codOficinaCta").toString(), 3, '0')+
					StringUtils.leftPad(datos.obtenerInteger("numCuentaAux").toString(), 6, '0')+
					datos.obtenerInteger("digitoVerificadorCta").toString();
			logger.debug("Ejecutando sentencia SELECT SFBDB AAMPR, parametro:" + datos.obtenerInteger("codProductoCta"));
			try {
				Map<String, Object> conceptoCuenta = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAMPR),datos.obtenerInteger("codProductoCta"));
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(conceptoCuenta);
				codConceptoProducto = adaptador.getInteger("codConceptoProducto");
				if(UtileriaDeDatos.isEquals(codConceptoProducto, Constantes.CONCEPTO_AH)) {
					datos.agregarDato("codCausal", Constantes.AH_CAUSAL);
					acreditarCuentaAhorro(datos);
				}
				if(UtileriaDeDatos.isEquals(codConceptoProducto, Constantes.CONCEPTO_CC)) {
					logger.debug("Ejecutando sentencia SELECT SFBDB AAMTC, parametro: " + Constantes.GC_CODIGO_TIPO_CORRELATIVO + "-" 
							+ datos.obtenerInteger("codOficinaCta") + "-" 
							+ datos.obtenerInteger("codProductoCta"));
					try {
						Map<String, Object> conceptoCuentaCorriente = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAMTC), 
								Constantes.GC_CODIGO_TIPO_CORRELATIVO, datos.obtenerInteger("codOficinaCta"), 
								datos.obtenerInteger("codProductoCta"));	
						adaptador = UtileriaDeDatos.adaptarMapa(conceptoCuentaCorriente);
						senEncontrado = adaptador.getInteger("senEncontrado");
						datos.agregarDato("numDocum", adaptador.getInteger("numDocum"));
						if(UtileriaDeDatos.isEquals(senEncontrado, 1)) {
							numDocum = numDocum + 1;

							Object[] paramsAAMTC = {
									numDocum,
									Constantes.GC_CODIGO_TIPO_CORRELATIVO, 
									datos.obtenerInteger("codOficinaCta"), 
									datos.obtenerInteger("codProductoCta")
							};
							logger.debug("Ejecutando sentencia UPDATE SFBDB AAMTC, parametro: {}", Arrays.toString(paramsAAMTC));
							ejecutarSentencia(query(UPDATE_SFBDB_AAMTC), paramsAAMTC);
						}
						datos.agregarDato("codCausal", Constantes.CC_CAUSAL_DEPOSITO);
						datos.agregarDato("numDocumentoANB", numDocum);
						acreditarCuentaCorriente(datos);
					}catch (EmptyResultDataAccessException e2) {
					}
				}
			}catch (EmptyResultDataAccessException e) {
			}
		}
		datos.agregarDato("numCtaAAMSM", numCtaAAMSM);
	}
	
	/**
	 * M&eacutetodo auxiliar para Actualizar registro en tabla ICPGE
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void ActualizarRegistroTablaICPGE(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagoColectorGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagoColectorGenericoPeticion.class);
		
		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"),Constantes.NO)) {
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_DIFERENTE_CUOTA)) {

				Object[] params_ICPGE2 = {
						datos.obtenerInteger("codColector"), 
						datos.obtenerInteger("codTipoDocumento"),
						StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
				};
				
				Integer IFE_CARGA = 0;
				
				logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE3, parametros: {}", Arrays.toString(params_ICPGE2));
				IFE_CARGA = jdbcTemplate.queryForObject(query(SELECT_SFBDB_ICPGE3), Integer.class, params_ICPGE2);
				
				Object[] paramsICPGE = {
					datos.obtenerInteger("fechaSistemaAMD") , 
					"P",
					peticion.getCodColector(), 
					peticion.getCodTipoDocumento(), 
					StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
					IFE_CARGA,
				};
				
				logger.debug("Ejecutando sentencia UPDATE SFBDB ICPGE, parametro: {}", Arrays.toString(paramsICPGE));
				ejecutarSentencia(query(UPDATE_SFBDB_ICPGE), paramsICPGE);

			}
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para Insertar registro en tabla SFBDB_ICATR
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void insertarRegistroTablaSFBDB_ICATR(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		
		Long glbDtime = 0l;          	           
		String numCuentaSfb = "";
		Integer tipoClienteICPGE = 0;
		Integer fechaCarga = 0;
		Integer horaCarga = 0;
		String nomCliente = "";
		if(!UtileriaDeDatos.isNull(datos.obtenerValor("nomCliente"))){
			nomCliente = datos.obtenerString("nomCliente");
		}
		String numIdentificacionCliente = datos.obtenerString("numIdentificacionCliente");
		numCuentaSfb = datos.obtenerInteger("codProductoCta").toString()+datos.obtenerInteger("codOficinaCta").toString()+
				datos.obtenerInteger("numCuenta")+datos.obtenerInteger("digitoVerificadorCta");
		if(!UtileriaDeDatos.isNull(datos.obtenerValor("tipoClienteICPGE"))){
			tipoClienteICPGE = datos.obtenerInteger("tipoClienteICPGE");
		}
		
		if(!UtileriaDeDatos.isNull(datos.obtenerValor("fechaCarga"))){
			fechaCarga = datos.obtenerInteger("fechaCarga");
		}
		if(!UtileriaDeDatos.isNull(datos.obtenerValor("horaCarga"))){
			horaCarga = datos.obtenerInteger("horaCarga");
		}
		

		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"), Constantes.NO)) {

			glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME_DIF), Long.class);
			if(UtileriaDeDatos.isBlank(numCuentaSfb) || UtileriaDeDatos.isEquals(numCuentaSfb, "0")){
				numCuentaSfb="";
			}
			
			Object[] paramsICATR = {
				glbDtime, 
				datos.obtenerInteger("codColector"),
				"P", 
				datos.obtenerInteger("codTipoDocumento"), 
				datos.obtenerInteger("codOficinaTran"), 
				datos.obtenerInteger("codTerminal") ,
				datos.obtenerString("codCajero"),
				tipoClienteICPGE,
				datos.obtenerInteger("fechaSistemaAMD") ,
				datos.obtenerInteger("horaSistema"),
				datos.obtenerBigDecimal("valorMovimiento") ,
				nomCliente,
				numIdentificacionCliente, 
				fechaCarga,
				horaCarga,
				datos.obtenerString("numCredito"),
				datos.obtenerInteger("numDocumentoTran"),
				datos.obtenerInteger("numDocumentoTran"),
				Constantes.NO, 
				datos.obtenerInteger("codTipoIdentificacion"),
				datos.obtenerInteger("fechaVencimiento"), 
				datos.obtenerString("periodoDeclaracion"),
				datos.obtenerString("numCtaAAMSM"),
				datos.obtenerBigDecimal("valorCheque"),
				0,
				0,
				0,
				0
			};
			
			logger.debug("Ejecutando sentencia INSERT_SFBDB_ICATR, parametro: {}", Arrays.toString(paramsICATR));
			ejecutarSentencia(query(INSERT_SFBDB_ICATR), paramsICATR);
			
			////////////////////
			////////////////////
			Object[] paramsPTR = {
					datos.obtenerInteger("fechaSistemaAMD") ,
					datos.obtenerInteger("codOficinaTran"), 
					datos.obtenerInteger("codTerminal") ,
					datos.obtenerString("codCajero"),	
					datos.obtenerInteger("numDocumentoTran"),
					glbDtime
					
			};
			
			logger.debug("Ejecutando sentencia INSERT SFBDB P ICATR, parametros: {}", Arrays.toString(paramsPTR));
			ejecutarSentencia(query(INSERT_SFBDB_P_ICATR), paramsPTR);
			
			Object[] paramsPTR01 = {
					new Integer(0),
					datos.obtenerInteger("fechaSistemaAMD"),
					datos.obtenerInteger("codColector"),				 
					datos.obtenerInteger("codTipoDocumento"),
					datos.obtenerInteger("numDocumentoTran"),
					glbDtime
					
			};
			
			logger.debug("Ejecutando sentencia INSERT SFBDB P ICATR01, parametros: {}", Arrays.toString(paramsPTR01));
			ejecutarSentencia(query(INSERT_SFBDB_P_ICATR01), paramsPTR01);

			///////////////////
			//////////////////
			
		}
		
		numIdentificacionCliente = "";
		String nombreCliente = "";
		String numeroCredito = "";
		
		
		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"), Constantes.SI)) {
			logger.debug("Ejecutando sentencia SELECT SFBDB AAACM, parametro: " + datos.obtenerString("codCajero") + "-" + datos.obtenerInteger("numLote") + "-" + Constantes.GC_ESTADO_COLECTOR_PENDIENTE);
			try {
				List<Map<String, Object>> listadoCargasArchivo = jdbcTemplate.queryForList(query(SELECT_SFBDB_AAACM),datos.obtenerString("codCajero") , datos.obtenerInteger("numLote") , Constantes.GC_ESTADO_COLECTOR_PENDIENTE);
				Object[] paramsSFBDB_P_ICATR = new Object[6];
				Object[] paramsSFBDB_P_ICATR01 = new Object[6];
				for(Map<String, Object> map : listadoCargasArchivo) {
					AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map);
					glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME_DIF), Long.class);
					numIdentificacionCliente = adaptador.getString("numRegArchivoCargado").substring(11, 26);
					nombreCliente = adaptador.getString("numRegArchivoCargado").substring(75, 120);
					numeroCredito = adaptador.getString("numRegArchivoCargado").substring(25, 46);
					datos.agregarDato("numIdentificacionCliente", numIdentificacionCliente);
					datos.agregarDato("nombreCliente", nombreCliente);
					datos.agregarDato("numeroCredito", numeroCredito);
				
				Object[] paramsICATR = {
					glbDtime , datos.obtenerInteger("codColector") , "P" , 
					datos.obtenerInteger("codTipoDocumento") , datos.obtenerInteger("codOficinaTran") , datos.obtenerInteger("codTerminalTran") ,
					datos.obtenerString("codCajero")  ,datos.obtenerInteger("tipoClienteICPGE"),datos.obtenerInteger("fechaSistemaAMD") ,
					datos.obtenerInteger("horaSistema"),datos.obtenerBigDecimal("valorMovimiento") , datos.obtenerString("nomCliente") ,
					datos.obtenerInteger("numIdentificacionCliente") , datos.obtenerInteger("fechaCarga") ,datos.obtenerInteger("horaCarga") ,
					datos.obtenerString("numCredito") ,datos.obtenerInteger("numDocumentoTran") ,datos.obtenerInteger("numDocumentoTran"),
					Constantes.NO , datos.obtenerString("codTipoIdentificacion") ,  datos.obtenerInteger("fechaVencimiento") , 
					datos.obtenerString("periodoDeclaracion"), numCuentaSfb,  datos.obtenerBigDecimal("valorMovimiento")
				};
					logger.debug("Ejecutando sentencia INSERT_SFBDB_ICATR, parametro: {}", Arrays.toString(paramsICATR));
					ejecutarSentencia(query(INSERT_SFBDB_ICATR), paramsICATR);
					
					///////////////
					///////////////
					paramsSFBDB_P_ICATR[0] = datos.obtenerInteger("fechaSistemaAMD");
					paramsSFBDB_P_ICATR[1] = datos.obtenerInteger("codOficinaTran");
					paramsSFBDB_P_ICATR[2] = datos.obtenerInteger("codTerminalTran");
					paramsSFBDB_P_ICATR[3] = datos.obtenerString("codCajero");
					paramsSFBDB_P_ICATR[4] = datos.obtenerInteger("numDocumentoTran");
					paramsSFBDB_P_ICATR[5] = glbDtime;					
					logger.debug("Ejecutando sentencia INSERT SFBDB P ICATR, parametros: {}", Arrays.toString(paramsSFBDB_P_ICATR));
					ejecutarSentencia(query(INSERT_SFBDB_P_ICATR), paramsSFBDB_P_ICATR);					
					paramsSFBDB_P_ICATR01[0] = new Integer(0);
					paramsSFBDB_P_ICATR01[1] = datos.obtenerInteger("fechaSistemaAMD");
					paramsSFBDB_P_ICATR01[2] = datos.obtenerInteger("codColector");
					paramsSFBDB_P_ICATR01[3] = datos.obtenerInteger("codTipoDocumento");
					paramsSFBDB_P_ICATR01[4] = datos.obtenerInteger("numDocumentoTran");
					paramsSFBDB_P_ICATR01[5] = glbDtime;				
					logger.debug("Ejecutando sentencia INSERT SFBDB P ICATR01, parametros: {}", Arrays.toString(paramsSFBDB_P_ICATR01));
					ejecutarSentencia(query(INSERT_SFBDB_P_ICATR01), paramsSFBDB_P_ICATR01);
					
					///////////////
					///////////////

				}
			}catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20019, "No Existe {}", "ARCHIVO CARGADO");
			}
			
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para Asignación de variables de salida
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void AsignaciónVariablesSalida(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		String periodoDeclaracion = datos.obtenerString("periodoDeclaracion");
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		String nomOficinaTran = datos.obtenerString("nomOficinaTran");
		String aliasIEMCO = datos.obtenerString("aliasIEMCO");
		datos.agregarDato("descTipoDocumento", aliasIEMCO);
		datos.agregarDato("periodoPago", periodoDeclaracion);
		datos.agregarDato("fechaTransaccion", fechaSistema);
		datos.agregarDato("nomAgencia", nomOficinaTran);
		//periodoDeclaracion recuperada de obtener Parametros Colector
		//fechaSistema recuperada de Seguridad Terminales Financieros
		//nom oficina recuperada de Seguridad Terminales Financieros
		//hora sistema recuperada de Seguridad Terminales Financieros
		//codCausal recuperada de validar Relacion Colector Con Documento De Pago
		//codSubCausal recuperada de validar Relacion Colector Con Documento De Pago
	}
	
}

