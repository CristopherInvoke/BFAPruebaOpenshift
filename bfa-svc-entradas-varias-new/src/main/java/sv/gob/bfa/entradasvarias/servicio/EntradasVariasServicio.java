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
import sv.gob.bfa.entradasvarias.funcionesinternas.RegistrarInscripcionRenovacionProduceSeguro;
import sv.gob.bfa.entradasvarias.funcionesinternas.ValidarFondosSubsidioPrimaSeguro;
import sv.gob.bfa.entradasvarias.model.EntradasVariasPeticion;
import sv.gob.bfa.entradasvarias.model.EntradasVariasRespuesta;

/**
 * Clase contiene logica del negocio correspondiente a servicio Entradas varias AJ201.
 */
public class EntradasVariasServicio extends Servicio{
	
	private static final String NOM_COD_SERVICIO = "Entradas varias AJ201: ";
	
	private final static String SELECT_SFBDB_BSMTG= "SELECT	ano_larga as nomTipDocumentoPersona" + 
			"	FROM linc.sfbdb_bsmtg@DBLINK@" + 
			"	WHERE aco_tabla = ?" + 
			"	AND  aco_codig = LPAD(?, 2, '0')";
	
	
	private final static String SELECT_SFBDB_OIMSI = "select oseadmenv as senAdmEnvioExterior," + 
	"	oco_servi as codServicioOIMSI" + 
	"	from linc.sfbdb_oimsi@DBLINK@" + 
	"	where ocotipser = ?" + 
	"	and ococauenv = ?";
	
	private final static String SELECT_EXIST_NUM_OPER_INT = "select ocotipser as codTipoServicio," + 
			"	ocoperloc as codCliente," +
			"   ofuperloc as senClientePersona, " +
			"   oco_servi as codServicioOIMOI," + 
			"   oseregbor as senRegistroEliminado," + 
			"   ocotipope as codTipoOperacion," + 
			"   oco_estad as codEstadoRegistro," + 
			"   ova_aplic as valorOperacionInternacional," + 
			"   dco_ofici as codOficinaOIMOI," + 
			"   ocotidoloc as tipDocumentoLocal," + 
			"   onudocloc as numDocumentoLocal," + 
			"   ocopaiext as codPaisExterno     " + 
			"  from linc.sfbdb_oimoi@DBLINK@" + 
			" where onuopeint = ?" ;
	
	private final static String SELECT_TIPO_DOCUMENTO = "select ano_corta AS nombreDocumentoCliente" + 
			"	from linc.sfbdb_bsmtg@DBLINK@" + 
			"	where aco_tabla= ?" + 
			"	and aco_codig = LPAD(?, 2, '0')"; 
	
	private final static String SELECT_PAIS_SFBDB_BSMTG = 
            "SELECT ANO_CORTA AS nombrePaisDestino" +
            "       FROM LINC.SFBDB_BSMTG@DBLINK@" +
            "       WHERE ACO_TABLA= ?" +
            "       AND ACO_CODIG= TO_CHAR(?)" ;
	
	private final static String SELECT_SFBDB_IEACS = "select tva_movim as valorMovimientoIEACS," + 
			"	ife_trans as fechaTransaccionIEACS, " + 
			"	ico_pagapl as codPagoAplicacion," + 
			"	ico_tippag as codTipoPago" + 
			"	from linc.sfbdb_ieacs@DBLINK@" + 
			"	where tnudoctra = ?"; 
	
	private final static String SELECT_SFBDB_PPRSP = "Select pvasubbfa as valorSubsidioBFA," + 
			"   pva_prima as valorPrimaSeguro," + 
			"   pmo_segur as montoAsegurado," + 
			"   pse_renov as senRenovacion," + 
			"   pfevenpol as fechaVencePoliza," + 
			"   pfe_suscr as fecSuscripcion," + 
			"   pferenpol as fecRenuevaPoliza," + 
			"   pcoconesp as codFondoContribEspecial," + 
			"   pcopagseg as codPagoSeguro," + 
			"   glb_dtime as glbDtimePPRSP," + 
			"   pcoempseg as codEmpresaAseguradora" + 
			"	from linc.sfbdb_pprsp@DBLINK@" + 
			"	where pcu_ofici = ?" + 
			"   and pcu_produ = ?" + 
			"   and pcunumcue = ?" + 
			"   and pcoempseg = ?" + 
			"   and pcotipseg = ?";
	
	private final static String SELECT_SFBDB_BSMBS= "select spo_cober AS porcentajeCobertura" + 
			"	from linc.sfbdb_bsmbs@DBLINK@" + 
			"	where sco_ident = ?" + 
			"   and pcu_ofici = ?" + 
			"   and pcu_produ = ?" + 
			"   and pcunumcue = ?";
	
	private final static String UPDATE_PRIMA_Y_MONTO = "UPDATE LINC.SFBDB_PPRSP@DBLINK@" + 
			" SET PVA_PRIMA = ?," + 
			" PVASUBBFA = ?" + 
			" WHERE GLB_DTIME = ?";
	
	private final static String UPDATE_SFBDB_PPRSP = "update linc.sfbdb_pprsp@DBLINK@" + 
			"	set pde_accion = ?," + 
			"	aco_usuar  = ?," + 
			"	pfe_accion = ?" + 
			"	where glb_dtime = ?" ;
	
	private final static String SELECT_GLBDTIME_DIF = "SELECT MADMIN.GENERATE_GLBDTIME_DIF as glbDtimeDAALA FROM DUAL";
	
	private final static String INSERT_SFBDB_PPALS = 
			"INSERT INTO LINC.SFBDB_PPALS@DBLINK@( " + 
			"ACO_ACCION," + 
			"ACO_USUAR," + 
			"DCO_ISPEC," + 
			"GLB_DTIME," + 
			"PCOEMPSEG," + 
			"PCOTIPSEG," + 
			"PCU_OFICI," + 
			"PCU_PRODU," + 
			"PCUDIGVER," + 
			"PCUNUMCUE," + 
			"PFE_CREAC," + 
			"PFE_ELISEG," + 
			"PFE_FINPON," + 
			"PFE_INIPON," + 
			"PFE_MODIF," + 
			"PFE_RENPON," + 
			"PHO_CREAC," + 
			"PHO_MODIF," + 
			"PMO_SEGURO," + 
			"PNO_DESCR," + 
			"PVA_VALOR" + 
			")" + 
			"VALUES (" + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?," + 
			"?)";
	
	
	private static final String UPDATE_SFBDB_IEACS = "update linc.sfbdb_ieacs@DBLINK@" + 
			"	set ico_pagapl = ?," + 
			"	dco_ofici  = ?," + 
			"	dco_termi  = ?," + 
			"	dco_usuar  = ?," + 
			"	tho_trans  = ?," + 
			"	tfe_pago   = ?" + 
			"	where tnudoctra  = ?";
	
	private static final String UPDATE_INSCRIPCION_SEG = "update linc.sfbdb_pprsp@DBLINK@" + 
			"	set pfe_suscr = ? ," + 
			"	pfevenpol = ?" + 
			"	where glb_dtime = ?";
	
	
	private static final String UPDATE_RENOVACION_SEG = "update linc.sfbdb_pprsp@DBLINK@" + 
			"	set pfevenpol = ?," + 
			"	pferenpol = ?" + 
			"	where glb_dtime = ?";
	
	private static final String UPDATE_SFBDB_OIMOI = "update linc.sfbdb_oimoi@DBLINK@" + 
			"	set oco_estad = ?," + 
			"	ocousupro = ?," + 
			"	ofe_proce = ?," + 
			"	oho_proce = ?" + 
			"	where onuopeint = ?";
	
	private static final String SELECT_NUM_TRAN = "SELECT MADMIN.FNC_CORREL_CANAL( ? ) as numTran FROM DUAL";
	
	private static final String UPDATE_SFBDB_AAARP = "UPDATE  linc.sfbdb_aaarp@DBLINK@" + 
			"	SET aco_causa =  ?" + 
			"	aco_conce   = ?," + 
			"	dcoteradi   = ?," + 
			"	dcoteruso   = ?," + 
			"	sco_estad   = ?," + 
			"	scoofiuso   = ?," + 
			"	scousuuso   = ?," + 
			"	sfe_uso     = ?," + 
			"	sho_uso     = ?," + 
			"	tnudoctr2   = ?," + 
			"	tnudoctra   = ?," + 
			"	tva_efect   = ?," + 
			"	tva_movim   = ?," + 
			"	tva_valor   = ?," + 
			"	tnu_trans   = ?" + 
			"	WHERE  scoregpre  = ?";
	
	private static final String SELECT_SFBDB_DXMTR ="select dco_trans as codTransaccionSubsidio," + 
			"	dcodebcre as codDebcreSubsidio," + 
			"	dco_ispec as codPantallaSubsidio" + 
			"	from linc.sfbdb_dxmtr@DBLINK@" + 
			"	where dco_ispec = ?";
	
	private static final String SELECT_SFBDB_PPRGP = "select MAX(GLB_DTIME) as glbDtimeGastoBFA " + 
			"   from linc.sfbdb_pprgp@DBLINK@" + 
			"   where pcu_ofici = ?" + 
			"   and pcu_produ = ?" + 
			"   and pcunumcue = ?" + 
			"	and pco_gasto = ?";
	
	public static final String SELECT_COD_ESTADO = "select pco_estad as codEstadoGasto" + 
			"	from linc.sfbdb_pprgp@DBLINK@" + 
			"	where glb_dtime = ? ";
	
	public static final String UPDATE_SFBDB_PPRGP = "update linc.sfbdb_pprgp@DBLINK@" + 
			"	set pco_estad = ?" + 
			"	where glb_dtime = ?";
	
	private static final String SELECT_SFBDB_PPACE = "select ase_saldo as senDisponibilidadSaldo" + 
			"	from linc.sfbdb_ppace@DBLINK@" + 
			"	where pcoconesp = ?";
	
	
	Logger logger = LoggerFactory.getLogger(EntradasVariasServicio.class);
	
	/**
	 * M&eacutetodo principal, contiene toda la logica del negocio
	 */
	@Override
	public Object procesar(Object objetoDom) throws ServicioException{
		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");
		
		logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> EntradasVariasPeticion");
		EntradasVariasPeticion peticion = (EntradasVariasPeticion) objetoDom;
		try {
			
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			validacionInicial(peticion);
			
			Integer codProductoCta = 0;
			if(!UtileriaDeDatos.isNull(peticion.getCuentaTransaccion()) &&
			   !UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion()) &&
			   !UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) &&
			   !UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO)
				) {
			codProductoCta = Integer.parseInt(peticion.getCuentaTransaccion().substring(0, 3));
			}
			datos.agregarDato("codProducto", codProductoCta);
			datos.agregarDato("peticion",peticion);
			datos.agregarPropiedadesDeObjeto(peticion);
			
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			seguridadTerminalFinancieros(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando datos cuenta y cliente");
			recuperarDatosCuenta(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Definiendo variables que se usaran en el proceso");
			datos.agregarDato("codConcepto", Constantes.CONCEPTO_VE);
			datos.agregarDato("codDebCre", Constantes.CREDITO);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando datos cuenta y cliente");
			recuperarDocumentoTransaccion(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar datos de operaciones internacionales");
			validarDatosOperInternacionales(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar datos préstamos honrados y saneados");
			validarDatosPrestamosHS(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar causal inscripcion/renovacion de seguro");
			validarCausal(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validacion Registro UIF");
			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.OI_CAUSAL_INGRESO_REMESA_RIA) || 
					UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.OI_CAUSAL_INGRESO_REMESA_MONEY_GRAM) ||
					UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) ||
					UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO) ||
					UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_HONRADO_ABONO_EFECTIVO)) {
			validarRegistroUIF(datos);
			}
			
			logger.debug(NOM_COD_SERVICIO + "Actualizando datos del prestamo");
			actualizarDatosPrestamo(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Actualizando datos inscripcion/ renovacion seguro");
			inscripcionYRenovacionSeguro(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Actualizando operaciones internacionales");
			actualizarOperInternacionales(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando numero de transaccion");
			recuperarNumTran(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Actualizando datos registros UIF");
			actualizarDatosUIF(datos);

			logger.debug(NOM_COD_SERVICIO + "Registrando en tabla AAATR");
			registarAAATR(datos);
			
			grabarGasto(datos);
			
			EntradasVariasRespuesta respuesta = new EntradasVariasRespuesta(); 
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			if(logger.isDebugEnabled()) {
				logger.debug(NOM_COD_SERVICIO + "RESPUESTA: {} ", respuesta);
			}
			
			return respuesta;
		
		} catch (ServicioException e) {
			logger.error("Error de servicio: " + e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(e);
		} catch (TipoDatoException | ParseException e) {
			logger.error("Error inesperado: " + e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, NOM_COD_SERVICIO + "Error inesperado: " + e.getMessage()));
		} 
	}
	
	/**
	 * M&eacutetodo auxiliar para validar peticion recibida
	 * @param peticion
	 * @throws ServicioException
	 */
	private void validacionInicial(EntradasVariasPeticion peticion) throws ServicioException {
		logger.debug(NOM_COD_SERVICIO + "Iniciando validacion de parametros");
		logger.debug(NOM_COD_SERVICIO + "Peticion recibida: {}", peticion);
		
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getSenSupervisor(), "senSupervisor", TipoValidacion.ENTERO_VALOR_EN, new Integer[] {1,2});
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodMoneda(), "codMoneda", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCausal(), "codCausal", TipoValidacion.ENTERO_MAYOR_CERO);
		if(!UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion())) {
			UtileriaDeParametros.validarParametro(peticion.getCuentaTransaccion(), "cuentaTransaccion", TipoValidacion.CADENA_NUMERICA);
		}
		UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumTransLavado(), "numTransLavado", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		
		if(!UtileriaDeDatos.isNull(peticion.getTipDocumentoPersona())) {
			UtileriaDeParametros.validarParametro(peticion.getTipDocumentoPersona(), "tipDocumentoPersona", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		}
		
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodSubcausal(), "codSubcausal", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);

	}
	
	
	/**
	 * M&eacutetodo auxiliar para invocar la función de soporte seguridad para terminales financieros 
	 * @param peticion
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
	 * M&eacutetodo auxiliar para recuperar datos de la cuenta, segun el concepto de la cuenta 
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void recuperarDatosCuenta(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Cliente cliente = null;
		Integer codOficinaRecuperado = 0;
		Integer codProductoRecuperado = 0;
		Integer numCuentaRecuperado = 0;
		Integer digitoVerificadorRecuperado = 0;
		
		
		if( 
			!UtileriaDeDatos.isNull(peticion.getCuentaTransaccion()) && 
		    !UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion()) &&
			!UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) &&
		    !UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO)
		   ) {
			Integer codProducto = datos.obtenerInteger("codProducto");
			Integer conceptoCuenta = Integer.parseInt(codProducto.toString().substring(0, 1));

			switch (conceptoCuenta) {
			case 1:

				CuentaCorriente pcc = recuperarDatosCuentaCorriente(peticion.getCuentaTransaccion());
				cliente = recuperarDatosCliente(pcc.getCodCliente());
				codOficinaRecuperado = pcc.getCodOficina();        
				codProductoRecuperado =  pcc.getCodProducto();                
				numCuentaRecuperado   =  pcc.getNumCuenta();         
				digitoVerificadorRecuperado = pcc.getDigitoVerificador();
				datos.agregarDato("pcc", pcc);
				break;

			case 2:

				CuentaAhorro pca = recuperarDatosCuentaAhorro(peticion.getCuentaTransaccion());
				cliente = recuperarDatosCliente(pca.getCodCliente());
				codOficinaRecuperado = pca.getCodOficina();        
				codProductoRecuperado =  pca.getCodProducto();                
				numCuentaRecuperado   =  pca.getNumCuenta();         
				digitoVerificadorRecuperado = pca.getDigitoVerificador();
				datos.agregarDato("pca", pca);
				break;

			case 4:

				Certificado pce = recuperarDatosCuentaCertificado(peticion.getCuentaTransaccion());
				cliente = recuperarDatosCliente(pce.getCodCliente());
				codOficinaRecuperado = pce.getCodOficina();        
				codProductoRecuperado =  pce.getCodProducto();                
				numCuentaRecuperado   =  pce.getNumCuenta();         
				digitoVerificadorRecuperado = pce.getDigitoVerificador();
				datos.agregarDato("pce", pce);
				break;

			case 6:

				CuentaPrestamo pcp = recuperarDatosCuentaPrestamo(peticion.getCuentaTransaccion());
				cliente = recuperarDatosCliente(pcp.getCodCliente());
				codOficinaRecuperado = pcp.getCodOficina();        
				codProductoRecuperado =  pcp.getCodProducto();                
				numCuentaRecuperado   =  pcp.getNumCuenta();         
				digitoVerificadorRecuperado = pcp.getDigitoVerificador();
				datos.agregarDato("pcp", pcp);
				break;
			}
		}
		
		if (!UtileriaDeDatos.isNull(cliente)) {
			datos.agregarPropiedadesDeObjeto(cliente);
		}
		
		datos.agregarDato("cliente", cliente);
		datos.agregarDato("codOficinaRecuperado", codOficinaRecuperado);
		datos.agregarDato("codProductoRecuperado", codProductoRecuperado);
		datos.agregarDato("numCuentaRecuperado", numCuentaRecuperado);
		datos.agregarDato("digitoVerificadorRecuperado", digitoVerificadorRecuperado);

	}
	
	/**
	 * M&eacutetodo auxiliar para recuperar el documento de la persona que realiza la transacción
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void recuperarDocumentoTransaccion(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		String numDocumentoPersona = "";
		String nombrePersona = "";
		
		if(!UtileriaDeDatos.isNull(peticion.getNumDocumentoPersona())) {
			numDocumentoPersona = peticion.getNumDocumentoPersona();
		}
		
		if(!UtileriaDeDatos.isNull(peticion.getNombrePersona())) {
			nombrePersona = peticion.getNombrePersona();
		}

		Object[] paramsSFBDBSMTG = {
				"DOC-IDENTI",
				peticion.getTipDocumentoPersona(),
			};
		
		logger.debug("Ejecutando sentencia SELECT SFBDB BSMTG, parametros: " + Arrays.toString(paramsSFBDBSMTG));
		String nomTipDocumentoPersona = jdbcTemplate.queryForObject(query(SELECT_SFBDB_BSMTG), String.class, paramsSFBDBSMTG);	
		
		String descripcionTran = "COD.ID: " + peticion.getTipDocumentoPersona() +
								 " "  + nomTipDocumentoPersona +           
								 ": " +  numDocumentoPersona  +  
								 " NOMBRE TERCERO: " +  nombrePersona;
		
		datos.agregarDato("descripcionTran", descripcionTran);

	}
	
	/**
	 * M&eacutetodo auxiliar para validar datos de operaciones internacionales y verificar si el causal está asociado como operación internacional
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarDatosOperInternacionales(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {

		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Integer senOperacionInternacional = Constantes.NO;

		Object[] paramsSFBDOIMSI= {
				Constantes.OI_SERVICIO_REMESAS,
				peticion.getCodCausal(),
		};

		Map<String, Object> map_OIMSI = null;
		Integer senAdmEnvioExterior = null;
		Integer codServicioOIMSI = 0;
		Map<String, Object> map_NumOperInter = null;

		Integer codTipoServicio = 0;
		Integer codServicioOIMOI = 0;
		Integer senRegistroEliminado = 0;
		String codTipoOperacion = "";
		String codEstadoRegistro = "";
		BigDecimal valorOperacionInternacional = BigDecimal.ZERO;
		Integer codOficinaOIMOI = 0;
		Integer tipDocumentoLocal = 0;
		String numDocumentoLocal = null;
		Integer codPaisExterno = 0;
		String codCliente = null;
		

		try {
			logger.debug("Ejecutando sentencia SELECT SFBDB BSMTG, parametros: " + Arrays.toString(paramsSFBDOIMSI));
			map_OIMSI  = jdbcTemplate.queryForMap(query(SELECT_SFBDB_OIMSI), paramsSFBDOIMSI);	

			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map_OIMSI);
			senAdmEnvioExterior = adaptador.getInteger("senAdmEnvioExterior");
			codServicioOIMSI = adaptador.getInteger("codServicioOIMSI");
			senOperacionInternacional = Constantes.SI;

		} catch (EmptyResultDataAccessException e) {
		}

		if((UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.OI_CAUSAL_INGRESO_REMESA_RIA) || 
				UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.OI_CAUSAL_INGRESO_REMESA_MONEY_GRAM)) &&
				!UtileriaDeDatos.isEquals(senOperacionInternacional, Constantes.SI)) {
			throw new ServicioException(20220, "Código de causal/transacción incompatibles");
		}

		logger.debug("Si es operacion internacional");
		if(UtileriaDeDatos.isEquals(senOperacionInternacional, Constantes.SI)) {

			if(UtileriaDeDatos.isEquals(senAdmEnvioExterior, Constantes.SI)) {
				throw new ServicioException(20108, "Tipo de Operacion Invalida {} ", " SEÑAL ADMINISTRATIVA");
			}

			try {
				logger.debug("Existencia de número de operación internacional");
				
				map_NumOperInter = this.jdbcTemplate.queryForMap(query(SELECT_EXIST_NUM_OPER_INT), peticion.getNumDocumentoTran());	
				AdaptadorDeMapa opInter = UtileriaDeDatos.adaptarMapa(map_NumOperInter);

				codTipoServicio = opInter.getInteger("codTipoServicio");
				codServicioOIMOI = opInter.getInteger("codServicioOIMOI");
				senRegistroEliminado = opInter.getInteger("senRegistroEliminado");
				codTipoOperacion = opInter.getString("codTipoOperacion");
				codEstadoRegistro = opInter.getString("codEstadoRegistro");
				valorOperacionInternacional = opInter.getBigDecimal("valorOperacionInternacional");
				codOficinaOIMOI = opInter.getInteger("codOficinaOIMOI");
				tipDocumentoLocal = opInter.getInteger("tipDocumentoLocal");
				numDocumentoLocal = opInter.getString("numDocumentoLocal");
				codPaisExterno = opInter.getInteger("codPaisExterno");
				
				if (UtileriaDeDatos.isEquals(opInter.getInteger("senClientePersona"),1)) {
					Cliente cliente = recuperarDatosCliente(opInter.getString("codCliente"));//cgonzalez
					datos.agregarDato("cliente", cliente);
					datos.agregarPropiedadesDeObjeto(cliente);
					datos.agregarDato("nombreCompletoCliente", datos.obtenerValor("nombreModificadoCliente"));
				}
				
				
			} catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20019, "No existe {} ", "DOCUMENTO EN OPERACION INTERNACIONAL") ;
			}

				logger.debug("Comprobación que el # de Op. corresponde al servicio");
				if(!UtileriaDeDatos.isEquals(codTipoServicio, Constantes.OI_SERVICIO_REMESAS)) {
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. INTERNACIONAL NO ES REMESA");
				}

				logger.debug("Comprobación que el # de Op. corresponde al servicio");
				if(!UtileriaDeDatos.isEquals(codServicioOIMOI, codServicioOIMSI)) {
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. INTERNACIONAL Y CAUSAL");
				}

				logger.debug("Comprobación que el # de registro no está borrado");
				if(UtileriaDeDatos.isEquals(senRegistroEliminado, Constantes.SI)) {
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. INERNACIONAL YA FUE ELIMINADA");
				}

				logger.debug("Comprobación del tipo de operación");
				if(!UtileriaDeDatos.isEquals(codTipoOperacion, Constantes.OI_ENVIO)) {
					throw new ServicioException(20108, "Tipo de Operacion Invalida {} ", " OP. INTERNACIONAL");
				}

				logger.debug("Comprobación del estado de la operación internacional 1.1");
				if(UtileriaDeDatos.isEquals(codEstadoRegistro, Constantes.OI_ESTADO_PROCESADO)) {
					throw new ServicioException(20016, "Estado Incorrecto {} ", " OP.INTERNACIONAL YA FUE PROCESADA") ;
				}

				logger.debug("Comprobación del estado de la operación internacional 2.1");
				if(!UtileriaDeDatos.isEquals(codEstadoRegistro, Constantes.OI_ESTADO_INGRESADO)) {
					throw new ServicioException(20016, "Estado Incorrecto {} ", " OP. INTERNACIONAL SIN PROCESAR") ;
				}

				logger.debug("Comprobación del monto de la operación");
				if(!UtileriaDeDatos.isEquals(peticion.getValorEfectivo(), valorOperacionInternacional)) {
					throw new ServicioException(20224, "Valor de la transacción incorrecta");
				}

				logger.debug("Comprobación que la operación sea de la misma oficina");
				if(!UtileriaDeDatos.isEquals(codOficinaOIMOI, peticion.getCodOficinaTran())) {
					throw new ServicioException(20589, "Numero incorrecto {}", "OP. ES DE OTRA AGENCIA");
				}

				logger.debug("Tipo documento válido");

				Object[] paramsSFBDBSMTG = {
						"DOC-VIGFIN",
						peticion.getTipDocumentoPersona(),
				};

				try {

					logger.debug("Ejecutando sentencia SELECT SFBDB BSMTG, parametros: " + Arrays.toString(paramsSFBDBSMTG));
					String nombreDocumentoCliente = jdbcTemplate.queryForObject(query(SELECT_TIPO_DOCUMENTO), String.class, paramsSFBDBSMTG);	

				} catch (EmptyResultDataAccessException e) {
					throw new ServicioException(20019, "No existe {} ", "TIPO DE DOCUMENTO") ;
				}
				
				logger.debug("Comparando documento al presentado en plataforma");
				if(!UtileriaDeDatos.isEquals(peticion.getTipDocumentoPersona(), tipDocumentoLocal) ||
				   !UtileriaDeDatos.isEquals(peticion.getNumDocumentoPersona(), numDocumentoLocal)) {
					throw new ServicioException(20589, "Numero incorrecto {}", "DOCUMENTO ES DIFERENTE");
				}

				logger.debug("Validando pais de residencia");
				if(UtileriaDeDatos.isGreaterThanZero(codPaisExterno)) {

					Object[] params_SFBDBSMTG = {
							"PAISES",
							codPaisExterno.toString(),
					};
					
					try {
				        logger.debug("Ejecutando sentencia SELECT SFBDB BSMTG, parametros: " + Arrays.toString(paramsSFBDBSMTG));
				        String nombrePaisDestino = jdbcTemplate.queryForObject(query(SELECT_PAIS_SFBDB_BSMTG), String.class, params_SFBDBSMTG);
				        datos.agregarDato("nombrePaisDestino", nombrePaisDestino);
				   } catch (EmptyResultDataAccessException e) {
						throw new ServicioException(20291, "TIPO DE TABLA NO EXISTE {} ", "- PAIS ORIGEN REMITENTE") ;

				   }

				}

				logger.debug("Asignaion de variables para funcion de soporte");
				String fechaAMD = datos.obtenerInteger("fechaSistemaAMD").toString();
				
		        Integer diafechaSistema = Integer.parseInt(fechaAMD.substring(6,8));
		        Integer mesfechaSistema = Integer.parseInt(fechaAMD.substring(4,6));
		        Integer aniofechaSistema = Integer.parseInt(fechaAMD.substring(0,4));

				logger.debug("Se valida por tipo de operación internacional");
				datos.agregarDato("diaTran", diafechaSistema);
				datos.agregarDato("mesTran", mesfechaSistema);
				datos.agregarDato("anioTran", aniofechaSistema);
				datos.agregarDato("numDocumentoLocal", numDocumentoLocal);
				datos.agregarDato("codTipoDocumentoLocal", tipDocumentoLocal);
				datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
				datos.agregarDato("codTipoServicio", Constantes.OI_SERVICIO_REMESAS);

				logger.debug("Llamando a funcion de soporte validarLimitesOI)");
				validarLimitesOI(datos);

//				logger.debug("Se valida para todos los tipos de operación internacional");
//				datos.agregarDato("diaTran", diafechaSistema);
//				datos.agregarDato("mesTran", mesfechaSistema);
//				datos.agregarDato("anioTran", aniofechaSistema);
//				datos.agregarDato("numDocumentoLocal", numDocumentoLocal);
//				datos.agregarDato("codTipoDocumentoLocal", tipDocumentoLocal);
//				datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
//				datos.agregarDato("codTipoServicio", new Integer(0));
//				logger.debug("Llamando a funcion de soporte validarLimitesOI)");
//				validarLimitesOI(datos);
				
				datos.agregarDato("senOperacionInternacional", senOperacionInternacional);
		}
		
		datos.agregarDato("senOperacionInternacional", senOperacionInternacional);
		datos.agregarDato("senAdmEnvioExterior", senAdmEnvioExterior);
		
	}
	
	
	/**
	 * M&eacutetodo auxiliar para validar datos de préstamos honrados y saneados
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarDatosPrestamosHS(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Integer fechaSistemaAMD = datos.obtenerInteger("fechaSistemaAMD");
		Map<String, Object> map_sfbdb_ieacs = null;
		
		BigDecimal valorMovimientoIEACS = BigDecimal.ZERO;
		Integer fechaTransaccionIEACS = 0;
		Integer codPagoAplicacion = 0;
		Integer codTipoPago = 0;
		

		if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) ||
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO) ||
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_HONRADO_ABONO_EFECTIVO) || 
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_HONRADO_ABONO_FSG)) {
			
			try {
				
				map_sfbdb_ieacs = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEACS), peticion.getNumDocumentoTran());	
				
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map_sfbdb_ieacs);
				valorMovimientoIEACS = adaptador.getBigDecimal("valorMovimientoIEACS");
				fechaTransaccionIEACS = adaptador.getInteger("fechaTransaccionIEACS");
				codPagoAplicacion = adaptador.getInteger("codPagoAplicacion");
				codTipoPago = adaptador.getInteger("codTipoPago");
				
			} catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20212, "Transacción no aparece en BD – CARSAN");
			}
			
			if(!UtileriaDeDatos.isEquals(valorMovimientoIEACS, peticion.getValorEfectivo())) {
				throw new ServicioException(20018, "VALOR INCORRECTO {}", "NO COINCIDE CON EL MOVIMIENTO CARSAN");
			}
			
			if(!UtileriaDeDatos.isEquals(fechaTransaccionIEACS, fechaSistemaAMD)) {
				throw new ServicioException(20003, "FECHA INCORRECTA {}" , "NO COINCIDE CON EL MOVIMIENTO CARSAN");

			}
			
			if(!UtileriaDeDatos.isEquals(codPagoAplicacion, Constantes.PS_PAGO_PENDIENTE_APLICAR)) {
				throw new ServicioException(20016, "ESTADO INCORRECTO {} ", "ORDEN DE PAGO CARSAN") ;
			}
			
			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO) && 
			   !UtileriaDeDatos.isEquals(codTipoPago, Constantes.PP_PAGO_POR_DECRETO)) {
				throw new ServicioException(20282, "Código de causa incorrecto {} ", "DEBE SER CANCELACION POR DECRETO") ;
			}
			
			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) && 
			   !UtileriaDeDatos.isEquals(codTipoPago, Constantes.PP_PAGO_NORMAL)) {
				throw new ServicioException(20282, "Código de causa incorrecto {} ", "DEBE SER CANCELACION POR DECRETO") ;
			}
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para validar  si causal es inscripción o renovación de seguro
	 * para definir el tipo de seguro con el que está relacionado el pago.
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarCausal(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {

		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente", Cliente.class);
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		

		Integer codConcepto = datos.obtenerInteger("codConcepto");
		Integer codTipoSeguro = 0;
		BigDecimal valorPagaCliente = BigDecimal.ZERO;

		BigDecimal valorSubsidioBFA = BigDecimal.ZERO;
		BigDecimal valorPrimaSeguro = BigDecimal.ZERO;
		BigDecimal montoAsegurado = BigDecimal.ZERO;
		Integer senRenovacion = 0;
		Integer fechaVencePoliza = 0;
		Integer fecSuscripcion = 0;
		Integer fecRenuevaPoliza = 0;
		Integer codFondoContribEspecial = 0;
		Integer codEmpresaAseguradora = 0;
		Integer fechaInicioAMD = 0;
		Integer codPagoSeguro = 0;
		
		Long glbDtimePPRSP = null;
		BigDecimal porcentajeCobertura =  BigDecimal.ZERO;
		Date fechaVencimientoAMD = null;
		Map<String,Object> mapaPPRSP = null;
		Map<String, Object> mapaBSMBS = null;
		Integer fechaSistemaAMD =  datos.obtenerInteger("fechaSistemaAMD");
		
		if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_VIDA) || 
				UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_VIDA) ||
				UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_DANIO) ||
				UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_DANIO) ||
				UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_AUTO) ||
				UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_AUTO) ||
				UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_PRODUCE_SEGURO)) {

			logger.debug("Definiendo el código de tipo de seguro");
			codConcepto = Constantes.CONCEPTO_PP;
			datos.agregarDato("codEstadoPrestamo", pcp.getCodEstadoPrestamo());
			datos.agregarDato("codBloqueo", pcp.getCodBloqueo());
			validarEstadoPrestamos(datos);

			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_VIDA) || 
					UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_VIDA) ) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_VIDA;
			}

			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_DANIO) || 
					UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_DANIO) ) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_DANIOS;
			}

			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_AUTO) || 
					UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_AUTO) ) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_AUTO;
			}

			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_PRODUCE_SEGURO) ) {
				codTipoSeguro = Constantes.PS_TIPO_SEGURO_PRODUCE;
			}
			
			datos.agregarDato("codTipoSeguro", codTipoSeguro);

			logger.debug("Obteniendo el valor a pagar por el cliente");

			Object[] paramsPPRSP = {
					datos.obtenerInteger("codOficinaRecuperado"),
					datos.obtenerInteger("codProductoRecuperado"),
					datos.obtenerInteger("numCuentaRecuperado"),
					Constantes.PS_SEGURO_FUTURO,
					codTipoSeguro
			};

			try {
				mapaPPRSP = this.jdbcTemplate.queryForMap(query(SELECT_SFBDB_PPRSP), paramsPPRSP);

				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(mapaPPRSP);
				valorSubsidioBFA = adaptador.getBigDecimal("valorSubsidioBFA");
				valorPrimaSeguro = adaptador.getBigDecimal("valorPrimaSeguro").setScale(2, BigDecimal.ROUND_UP);
				montoAsegurado = adaptador.getBigDecimal("montoAsegurado");
				senRenovacion = adaptador.getInteger("senRenovacion");
				fechaVencePoliza = adaptador.getInteger("fechaVencePoliza");
				codFondoContribEspecial = adaptador.getInteger("codFondoContribEspecial");
				codPagoSeguro = adaptador.getInteger("codPagoSeguro");
				glbDtimePPRSP = adaptador.getLong("glbDtimePPRSP");
				codEmpresaAseguradora = adaptador.getInteger("codEmpresaAseguradora");
				
				valorPagaCliente = valorPrimaSeguro;
				valorPagaCliente = valorPagaCliente.subtract(valorSubsidioBFA);
				
				datos.agregarDato("senRenovacion", senRenovacion);
				datos.agregarDato("valorSubsidioBFA", valorSubsidioBFA);
				datos.agregarDato("glbDtimePPRSP", glbDtimePPRSP);
				datos.agregarDato("montoAsegurado", montoAsegurado);
				datos.agregarDato("fechaVencePoliza", fechaVencePoliza);

			}catch (EmptyResultDataAccessException erdae) {
				throw new ServicioException(20019, "No existe {} ", "RELACION DE LA CUENTA CON SEGUROS") ;
			}

			if(UtileriaDeDatos.isEquals(Constantes.PS_CAUSAL_PRODUCE_SEGURO, peticion.getCodCausal())) {
				logger.debug("Calculando porcentaje seguro");

				BigDecimal porcentajeSeguro = BigDecimal.ZERO;

				Object[] paramsBSMBS = {
						cliente.getCodCliente(),
						datos.obtenerInteger("codOficinaRecuperado"),
						datos.obtenerInteger("codProductoRecuperado"),
						datos.obtenerInteger("numCuentaRecuperado")
				};

				try {
					mapaBSMBS = this.jdbcTemplate.queryForMap(query(SELECT_SFBDB_BSMBS), paramsBSMBS);

					AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(mapaBSMBS);
					porcentajeCobertura = adaptador.getBigDecimal("porcentajeCobertura");
					porcentajeSeguro = porcentajeSeguro.add(porcentajeCobertura);

					if(!UtileriaDeDatos.isEquals(porcentajeSeguro, Constantes.PS_COBERTURA_COMPLETA)) {
						throw new ServicioException(20007, "Cantidad Incorrecta en sumatoria de porcentajes en BSMBS") ;
					}

				}catch (EmptyResultDataAccessException e) {
					throw new ServicioException(20019, "No existe {} ", "RELACION DE BENEF. PARA ESTE SEGURO") ;
				}
				
				
				datos.agregarDato("codProducto", datos.obtenerInteger("codProductoRecuperado"));
				datos.agregarDato("codOficina", datos.obtenerInteger("codOficinaRecuperado"));
				datos.agregarDato("numCuenta", datos.obtenerInteger("numCuentaRecuperado"));
				datos.agregarDato("codTipoSeguro", codTipoSeguro);
				datos.agregarDato("codFondoContribEspecial", codFondoContribEspecial);
				datos.agregarDato("fechaSistemaAMD", datos.obtenerInteger("fechaSistemaAMD"));
				//fechaSistema obtenido de Seguridad Terminales Financieros
				
				logger.debug("Llamando a la función calcular plazo seguro");
				calcularPlazoSeguro(datos);
				
				fechaVencimientoAMD = UtileriaDeDatos.fecha8ToDateyyyyMMdd(datos.obtenerInteger("fechaVencimientoAMD"));
				fechaInicioAMD = fechaSistemaAMD;
				BigDecimal tasaPrimaTipoSeguro = datos.obtenerBigDecimal("tasaPrimaTipoSeguro");
				BigDecimal montoLimiteSubsidio = datos.obtenerBigDecimal("montoLimiteSubsidio");
				Integer plazoMaximoDias = datos.obtenerInteger("plazoMaximoDias");
				Integer senDisponibilidadSaldo = null;

				try {
					senDisponibilidadSaldo = jdbcTemplate.queryForObject(query(SELECT_SFBDB_PPACE), Integer.class,codFondoContribEspecial);
				} catch (EmptyResultDataAccessException e) {
					throw new ServicioException(20019, "No existe {} ", "CODIGO DE SUBSIDIO") ;
				}
				
				
				datos.agregarDato("plazoMaximoDias", plazoMaximoDias);
//				diasPlazo recuperado de calcularPlazoSeguro
				datos.agregarDato("codPagoSeguro", codPagoSeguro);
				datos.agregarDato("codFondoContribEspecial", codFondoContribEspecial);
				datos.agregarDato("codDestino", pcp.getCodDestino());
				datos.agregarDato("montoAsegurado", montoAsegurado);
				datos.agregarDato("montoLimiteSubsidio", montoLimiteSubsidio);
				datos.agregarDato("senDisponibilidadSaldo", senDisponibilidadSaldo);
				datos.agregarDato("tasaPrimaTipoSeguro", tasaPrimaTipoSeguro);
				datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
				
				logger.debug("Validando fondos para el subsidio prima seguro");
				ValidarFondosSubsidioPrimaSeguro validarFondoSubsidio =  new ValidarFondosSubsidioPrimaSeguro(getJdbcTemplate(), getDbLinkValue());
				validarFondoSubsidio.validarFondosSubsidioPrimaSeguro(datos);
				

				logger.debug("Asignación de salidas de la función");
				BigDecimal montoSubsidiaBFA = datos.obtenerBigDecimal("montoSubsidiaBFA");
				BigDecimal valorPrima = datos.obtenerBigDecimal("valorPrima");

				logger.debug("Actualizando valor de la prima y monto subsidio BFA en la tabla PPRSP");

				Object[] params_PPRSP = {
						valorPrima,
						montoSubsidiaBFA,
						glbDtimePPRSP
				};

				ejecutarSentencia(query(UPDATE_PRIMA_Y_MONTO), params_PPRSP);

				
				logger.debug("Cálculo del valor que paga el cliente");
				valorPagaCliente = BigDecimal.ZERO;
				valorPagaCliente = valorPrima;
				valorPagaCliente = valorPagaCliente.subtract(montoSubsidiaBFA);
				
			}
			
			if(!UtileriaDeDatos.isEquals(valorPagaCliente, peticion.getValorEfectivo())) {
				throw new ServicioException(20018, "VALOR INCORRECTO {}", "LA PRIMA ES: " + valorPagaCliente + " - INGRESE ESTE VALOR");
			}
			
			logger.debug("Validando rango de edad para aplicar a seguro, si al invocar la función surge una excepción, se interrumpe el servicio.");
			
			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_VIDA)) {
				Integer edadMinimaSeguro = 0;
				Integer edadMaximaSeguro1 = Constantes.PS_EDAD_MAXIMA_SEGUROS;
				Integer edadMaximaSeguro2 = 0;
				
				datos.agregarDato("fechaNacimiento", cliente.getFechaNacimiento());
				datos.agregarDato("fechaReferencia", new Integer(0));
				datos.agregarDato("edadMinimaSeguro", edadMinimaSeguro);
				datos.agregarDato("edadMaximaSeguro1", edadMaximaSeguro1);
				datos.agregarDato("edadMaximaSeguro2", edadMaximaSeguro2);
//				montoAsegurado agregado anteriormente
				
				logger.debug("Llamando funcion para validar rango edad seguro");
				validaLimiteEdadSeguro(datos);
			}
			
			logger.debug("Si es inscripción de seguro");
			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_VIDA) || 
			   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_DANIO) ||
			   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_AUTO)) {
				
				logger.debug("Obteniendo la cantidad de días del año");
				
				Integer senBisiesto = 0;
				Integer diasAnio = 0;
				
				if(UtileriaDeDatos.anioEsBisiestoDMA(datos.obtenerInteger("fechaSistema"))) {
					senBisiesto = Constantes.SI;
				}

				if(UtileriaDeDatos.isEquals(senBisiesto, Constantes.SI)) {
					diasAnio = Constantes.PP_ANIO_BISIESTO;
				}else {
					diasAnio = Constantes.PP_ANIO_NO_BISIESTO;
				}
				
				logger.debug("Obteniendo valores a insertar");
				fechaVencimientoAMD =  UtileriaDeDatos.fechaSumDias(UtileriaDeDatos.fecha8ToDateyyyyMMdd(fechaSistemaAMD), diasAnio);
				
				Long glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME_DIF), Long.class);
				fechaInicioAMD = fechaSistemaAMD;
				
				logger.debug("Insertando registro en la tabla PPALS");
				Object[] params_PPALS = {
						Constantes.ACCION_MODIFICACION,
						peticion.getCodCajero(),
						datos.obtenerString("codPantalla"),
						glbDtime,
						codEmpresaAseguradora,
						codTipoSeguro,
						datos.obtenerInteger("codOficinaRecuperado"),
						datos.obtenerInteger("codProductoRecuperado"),
						datos.obtenerInteger("digitoVerificadorRecuperado"),
						datos.obtenerInteger("numCuentaRecuperado"),
						new Integer(0),
						new Integer(0),
						UtileriaDeDatos.tofecha8yyyyMMdd(fechaVencimientoAMD),
						fechaInicioAMD,
						datos.obtenerInteger("fechaSistema"),
						datos.obtenerInteger("fechaSistema"),
						new Integer(0),
						datos.obtenerInteger("horaSistema"),
						datos.obtenerBigDecimal("montoAsegurado"),
						"PAGO DE PRIMA EN EFECTIVO",
						valorPrimaSeguro
				};

				ejecutarSentencia(query(INSERT_SFBDB_PPALS), params_PPALS);
				
				logger.debug("Actualizando la tabla PPRSP");
				Object[] params_PPRSP = {
						"PAGO DE PRIMA EN EFECTIVO",
						peticion.getCodCajero(),
						datos.obtenerInteger("fechaSistema"),
						datos.obtenerLong("glbDtimePPRSP")
				};

				ejecutarSentencia(query(UPDATE_SFBDB_PPRSP), params_PPRSP);
				logger.debug("Fin si es inscripcion seguro");
			}
			
			logger.debug("Si es renovación de seguro");
			if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_VIDA) ||
			   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_AUTO) ||
			   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_DANIO)) {
				
				if(UtileriaDeDatos.isGreaterThanZero(fechaVencePoliza)) {
					
					logger.debug("Validando si la renovación de la póliza no excede a 90 días a la fecha de vencimiento");
					
					Date fechaVencePolizaAMD =  UtileriaDeDatos.fecha8ToDateyyyyMMdd(datos.obtenerInteger("fechaVencePoliza"));
					Date fechaSistemaDMA =  UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
					Integer fechaVencimientoPoliza = UtileriaDeDatos.toFecha6(fechaVencePolizaAMD);
					
					if(!UtileriaDeDatos.validarFechaDMA(fechaVencimientoPoliza)) {
						throw new ServicioException(20003, "Fecha incorrecta {}" , " DE VENCIMIENTO DE POLIZA");
					}
					
					Integer diasDiferencia = UtileriaDeDatos.diferenciaDiasFechas(fechaSistemaDMA, fechaVencePolizaAMD);
					
					if(UtileriaDeDatos.isGreater(diasDiferencia, new Integer(90))) {
						throw new ServicioException(20003, "Fecha incorrecta {}" , " POLIZA EXCEDE 90 DIAS PARA RENOVAR");
					}
					
					logger.debug("Validando si la fecha está en el rango de 15 días antes de renovar");
					Date fechaVencimientoProyectada =  UtileriaDeDatos.fechaSumDias(fechaSistemaDMA, 15);
					
					if(fechaVencePolizaAMD.compareTo(fechaVencimientoProyectada) > 0 ) {
						throw new ServicioException(20003, "Fecha incorrecta {}" , " FALTAN MAS DE 15 DIAS PARA RENOVAR");
					}
					
					Integer senBisiesto = 0;
					Integer diasAnio = 0;
					
					if(UtileriaDeDatos.anioEsBisiestoDMA(fechaVencimientoPoliza)) {
						senBisiesto = Constantes.SI;
					}

					if(UtileriaDeDatos.isEquals(senBisiesto, Constantes.SI)) {
						diasAnio = Constantes.PP_ANIO_BISIESTO;
					}else {
						diasAnio = Constantes.PP_ANIO_NO_BISIESTO;
					}
					
					logger.debug("Obteniendo valores a insertar");
					fechaVencimientoAMD =  UtileriaDeDatos.fechaSumDias(fechaVencePolizaAMD, diasAnio);
					
				}
				
				logger.debug("Definiendo valores a insertar");
				Long glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME_DIF), Long.class);
				
				Date fechaVencePolizaAMD =  UtileriaDeDatos.fecha8ToDateyyyyMMdd(datos.obtenerInteger("fechaVencePoliza"));
				fechaInicioAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaVencePolizaAMD);
				
				logger.debug("Insertar registro en la PPALS");
				Object[] params_PPALS = {
						Constantes.ACCION_MODIFICACION,
						peticion.getCodCajero(),
						datos.obtenerString("codPantalla"),
						glbDtime,
						codEmpresaAseguradora,
						codTipoSeguro,
						datos.obtenerInteger("codOficinaRecuperado"),
						datos.obtenerInteger("codProductoRecuperado"),
						datos.obtenerInteger("digitoVerificadorRecuperado"),
						datos.obtenerInteger("numCuentaRecuperado"),
						new Integer(0),
						new Integer(0),
						UtileriaDeDatos.tofecha8yyyyMMdd(fechaVencimientoAMD),
						fechaInicioAMD,
						datos.obtenerInteger("fechaSistema"),
						datos.obtenerInteger("fechaSistema"),
						new Integer(0),
						datos.obtenerInteger("horaSistema"),
						datos.obtenerBigDecimal("montoAsegurado"),
						"RENOVACION EN EFECTIVO",
						valorPrimaSeguro
				};
				
				logger.debug("Ejecutando sentencia INSERT SFBDB PPALS, parametros: {}", Arrays.toString(params_PPALS));
				ejecutarSentencia(query(INSERT_SFBDB_PPALS), params_PPALS);
				
				logger.debug("Actualizando la tabla PPRSP");
				Object[] params_PPRSP = {
						"RENOVACION EN EFECTIVO",
						peticion.getCodCajero(),
						datos.obtenerInteger("fechaSistema"),
						datos.obtenerLong("glbDtimePPRSP")
				};
				
				ejecutarSentencia(query(UPDATE_SFBDB_PPRSP), params_PPRSP);
				logger.debug("fin si renovacion seguro");
			}
			logger.debug("fin si Validar datos de Seguro Futuro");
		}
		if(UtileriaDeDatos.isNull(fechaVencimientoAMD)) {
			datos.agregarDato("fechaVencimientoAMD", 0);
		}else {
			datos.agregarDato("fechaVencimientoAMD", UtileriaDeDatos.tofecha8yyyyMMdd(fechaVencimientoAMD));
		}
		datos.agregarDato("fechaInicioAMD", fechaInicioAMD);
		datos.agregarDato("codConcepto", codConcepto);
		
	}
	
	/**
	 * M&eacutetodo auxiliar para actualizar datos de préstamos relacionados con CARSAN u Honrado
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void actualizarDatosPrestamo(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		
		Date fechaSistema = UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistema);
		
		if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_ABONO_EFECTIVO) ||
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_CARSAN_CANCELACION_DECRETO) || 
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_HONRADO_ABONO_EFECTIVO) ||
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PP_HONRADO_ABONO_FSG)) {
			
			Object[] params_IEACS = {
					Constantes.SI,
					peticion.getCodOficinaTran(),
					peticion.getCodTerminal(),
					peticion.getCodCajero(),
					datos.obtenerInteger("horaSistema"),
					fechaSistemaAMD,
					peticion.getNumDocumentoTran()
			};

			ejecutarSentencia(query(UPDATE_SFBDB_IEACS), params_IEACS);
		}
		
		datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
	}
	
	
	/**
	 * M&eacutetodo auxiliar para actualizar datos de préstamos relacionados con CARSAN o SEGURO FUTURO
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void inscripcionYRenovacionSeguro(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Integer fechaInicioAMD = datos.obtenerInteger("fechaInicioAMD");
		Integer fechaVencimientoAMD = datos.obtenerInteger("fechaVencimientoAMD");
		
		if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_VIDA) ||
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_DANIO) || 
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_INSCRIPCION_AUTO) ||
		   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_PRODUCE_SEGURO)) {
			

			Long glbDtimePPRSP  = datos.obtenerLong("glbDtimePPRSP");
			Object[] params_PPRSP = {
					fechaInicioAMD,
					fechaVencimientoAMD,
					glbDtimePPRSP
			};

			ejecutarSentencia(query(UPDATE_INSCRIPCION_SEG), params_PPRSP);
		}
		
		
		if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_VIDA) ||
				   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_AUTO) || 
				   UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_RENOV_DANIO)) {
					Integer fechaSistemaAMD = datos.obtenerInteger("fechaSistemaAMD");
					Long glbDtimePPRSP  = datos.obtenerLong("glbDtimePPRSP");
					Object[] params_PPRSP = {
							fechaVencimientoAMD,
							fechaSistemaAMD,
							glbDtimePPRSP
					};

					ejecutarSentencia(query(UPDATE_RENOVACION_SEG), params_PPRSP);
				}
	}
	
	
	/**
	 * M&eacutetodo auxiliar para actualizar las operaciones intenacionales
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void actualizarOperInternacionales(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Integer senOperacionInternacional = datos.obtenerInteger("senOperacionInternacional");
		Integer codTipoServicio = 0;
		
		Integer fechaSistema	= datos.obtenerInteger("fechaSistema");
		Date fechaSistemaD 		=  UtileriaDeDatos.fecha6ToDate(fechaSistema);
		Calendar calendar		= Calendar.getInstance();
		calendar.setTime(fechaSistemaD);
		Integer anio = calendar.get(Calendar.YEAR);
		Integer mes = calendar.get(Calendar.MONTH)+1;
		Integer dia = calendar.get(Calendar.DAY_OF_MONTH);
		
		if(UtileriaDeDatos.isEquals(senOperacionInternacional, Constantes.SI)) {
			Integer senAdmEnvioExterior = datos.obtenerInteger("senAdmEnvioExterior");
			
			logger.debug("Definiendo código de tipo de servicio");
			codTipoServicio = Constantes.OI_SERVICIO_REMESAS;
			
			if(!UtileriaDeDatos.isEquals(senAdmEnvioExterior, Constantes.SI)) {
				
				logger.debug("Por tipo de operación internacional");
				datos.agregarDato("codTipoServicio", codTipoServicio);
				datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
				datos.agregarDato("codTipoDocumentoLocal", peticion.getTipDocumentoPersona());
				datos.agregarDato("numDocumentoLocal", peticion.getNumDocumentoPersona());
				datos.agregarDato("anioTran", anio);
				datos.agregarDato("mesTran", mes);
				datos.agregarDato("diaTran", dia);
				datos.agregarDato("senReversa", Constantes.NO);
				acumularSaldoOI(datos);
				
				
				//cgonzalez 17/09/2020
				//Se puso opcional ya que al realizar operaciones internacionales no siempre la persona es un cliente.
				if (!UtileriaDeDatos.isNull(datos.obtenerValor("codCliente"))
						                           &&
					!UtileriaDeDatos.isBlank(datos.obtenerString("codCliente"))) {
					Cliente cliente = recuperarDatosCliente(datos.obtenerValor("codCliente").toString());	
					datos.agregarDato("cliente", cliente);
					datos.agregarPropiedadesDeObjeto(cliente);
				}
				
				datos.agregarDato("codTipoServicio", new Integer(0));
				acumularSaldoOI(datos);
				
			}
				
				Object[] paramsOIMOI = {
						Constantes.OI_ESTADO_PROCESADO,
						peticion.getCodCajero(),
						datos.obtenerInteger("fechaSistemaAMD"),
						datos.obtenerInteger("horaSistema"),
						peticion.getNumDocumentoTran()
					};
					
					logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB OIMOI, parametros: {}", Arrays.toString(paramsOIMOI));
					ejecutarSentencia(query(UPDATE_SFBDB_OIMOI), paramsOIMOI);
				
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para recuperar el numero de transaccion
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void recuperarNumTran(DatosOperacion datos) throws ServicioException {
		
		Integer numTran = this.jdbcTemplate.queryForObject(query(SELECT_NUM_TRAN), Integer.class, Constantes.VENTANILLA);
		datos.agregarDato("numTran", numTran);
		
	}
	
	/**
	 * M&eacutetodo auxiliar para actualizar Datos UIF si el parametro de entrada 
	 * corresponde al numero de transaccion de lavado de dinero es mayor a CERO
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void actualizarDatosUIF(DatosOperacion datos) throws ServicioException, TipoDatoException {
		
		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		
		if(UtileriaDeDatos.isGreaterThanZero(peticion.getNumTransLavado())) {
			
			Object[] params_AAARP = {
					peticion.getCodCausal(),
					Constantes.CONCEPTO_VE,
					datos.obtenerInteger("rpCodTerminalReg"),
					peticion.getCodTerminal(),
					Constantes.UIF_ESTADO_USADO,
					peticion.getCodOficinaTran(),
					peticion.getCodCajero(),
					datos.obtenerInteger("fechaSistemaAMD"),
					datos.obtenerInteger("horaSistema"),
					new Integer(0),
					peticion.getNumDocumentoTran(),
					peticion.getValorEfectivo(),
					peticion.getValorEfectivo(),
					new Integer(0),
					datos.obtenerInteger("numTran"),
					peticion.getNumTransLavado()
			};
			ejecutarSentencia(query(UPDATE_SFBDB_AAARP), params_AAARP);
		}
	}
	
	
	/**
	 * M&eacutetodo auxiliar para registrar en AAATR
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void registarAAATR(DatosOperacion datos) throws ServicioException, TipoDatoException {

		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente", Cliente.class);
		Integer codSectorEconomico = 0;
		String codCliente = " ";
		Integer tipDocumentoCliente = 0;
		String numDocumentoCliente = " ";
		
		if(!UtileriaDeDatos.isNull(cliente)) {
			codSectorEconomico = cliente.getCodSectorEconomicoCliente();
			codCliente = cliente.getCodCliente();
			tipDocumentoCliente = cliente.getTipDocumentoCliente();
			numDocumentoCliente = cliente.getNumDocumentoCliente();
		}
		
		if (UtileriaDeDatos.isNull(datos.obtenerValor("codOficinaRecuperado")) || 
			UtileriaDeDatos.isEquals(datos.obtenerInteger("codOficinaRecuperado"), new Integer(0))) {
			datos.agregarDato("codOficinaRecuperado", peticion.getCodOficinaTran());
		}
		
		if (UtileriaDeDatos.isNull(datos.obtenerValor("codProductoRecuperado"))) {
			datos.agregarDato("codProductoRecuperado", new Integer(0));
		}
		if (UtileriaDeDatos.isNull(datos.obtenerValor("numCuentaRecuperado"))) {
			datos.agregarDato("numCuentaRecuperado", new Integer(0));
		}
		if (UtileriaDeDatos.isNull(datos.obtenerValor("digitoVerificadorRecuperado"))) {
			datos.agregarDato("digitoVerificadorRecuperado", new Integer(0));
		}

		logger.debug("Preparando datos para registrar en tabla AAATR");
		datos.agregarDato("codCausal", peticion.getCodCausal());
		datos.agregarDato("codConcepto", Constantes.CONCEPTO_VE);
		datos.agregarDato("codOficina", datos.obtenerInteger("codOficinaRecuperado"));
		datos.agregarDato("codProducto", datos.obtenerInteger("codProductoRecuperado"));
		datos.agregarDato("numCuenta", datos.obtenerInteger("numCuentaRecuperado"));
		datos.agregarDato("digitoVerificador", datos.obtenerInteger("digitoVerificadorRecuperado"));
		datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
		datos.agregarDato("codTerminal", peticion.getCodTerminal());
		//fecha relativa recuperada de Seguridad Terminales Financieros
		//hora sistema recuperada de Seguridad Terminales Financieros
		datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
		datos.agregarDato("numTran", datos.obtenerInteger("numTran"));
		datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
		//codCompania recuperada de Seguridad Terminales Financieros
		datos.agregarDato("codMoneda", peticion.getCodMoneda());
		datos.agregarDato("numCaja", peticion.getNumCaja());
		datos.agregarDato("montoIVA", null);
		datos.agregarDato("codTran", peticion.getCodTran());
		datos.agregarDato("codCajero", peticion.getCodCajero());
		datos.agregarDato("codDebCre", datos.obtenerInteger("codDebCre"));
		datos.agregarDato("numSecuenciaCupon", null);
		datos.agregarDato("valorImpuestoVenta", null);
		datos.agregarDato("codSectorEconomico", codSectorEconomico );
		datos.agregarDato("numDiasAtras", null);
		//fechaSistema recuperada de Seguridad Terminales Financieros
		datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
		datos.agregarDato("numDocumentoReversa", null);
		datos.agregarDato("saldoAnterior", null);
		datos.agregarDato("senAJATR", Constantes.NO);
		datos.agregarDato("senAutorizacion", Constantes.NO);
		datos.agregarDato("senReversa", Constantes.NO);
		datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
		datos.agregarDato("senWANG", null);
		datos.agregarDato("senDiaAnterior", Constantes.NO);
		datos.agregarDato("senImpCaja", Constantes.SI);
		datos.agregarDato("senPosteo", Constantes.NO);
		datos.agregarDato("valorAnterior", BigDecimal.ZERO);
		datos.agregarDato("valorCompra", BigDecimal.ONE);
		datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
		datos.agregarDato("valorCheque", null);
		datos.agregarDato("valorVenta", BigDecimal.ONE);
		datos.agregarDato("numDocumentoTran2", null);
		datos.agregarDato("valorChequesAjenos", null);
		datos.agregarDato("valorChequesExt", null);
		datos.agregarDato("valorChequesPropios", null);
		datos.agregarDato("descripcionTran", datos.obtenerString("descripcionTran"));
		datos.agregarDato("codBancoTransf", null);
		datos.agregarDato("numCuentaTransf", "0000000000000");
		datos.agregarDato("codPaisTransf", null);
		datos.agregarDato("senACRM", Constantes.SI);
		datos.agregarDato("codCliente", codCliente);
		datos.agregarDato("valorImpuesto", null);
		datos.agregarDato("tipDocumentoCliente", tipDocumentoCliente);
		datos.agregarDato("numDocumentoCliente", numDocumentoCliente);
		datos.agregarDato("numDocumentoImp", null);
		datos.agregarDato("codSubCausal", peticion.getCodSubcausal());
		registrarTransaccionAAATR(datos);
	}
	
	
	/**
	 * M&eacutetodo auxiliar para registrar en AAATR
	 * @param peticion
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void grabarGasto(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {

		EntradasVariasPeticion peticion = datos.obtenerObjeto("peticion", EntradasVariasPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente", Cliente.class);
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		Integer codSectorEconomico = 0;
		String codCliente = "";
		Integer tipDocumentoCliente = 0;
		String numDocumentoCliente = "";
		
		if(!UtileriaDeDatos.isNull(cliente)) {
			codSectorEconomico = cliente.getCodSectorEconomicoCliente();
			codCliente = cliente.getCodCliente();
			tipDocumentoCliente = cliente.getTipDocumentoCliente();
			numDocumentoCliente = cliente.getNumDocumentoCliente();
		}
		
				
		if(UtileriaDeDatos.isEquals(peticion.getCodCausal(), Constantes.PS_CAUSAL_PRODUCE_SEGURO)) {
			
			logger.debug("Seteando variables para registro en AAATR");
			Integer codCausalGasto = Constantes.PS_CAUSAL_GASTO_PRODUCE_SEGURO;
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			
			logger.debug("Preparando datos para registrar en tabla AAATR");
			datos.agregarDato("codCausal", codCausalGasto);
			datos.agregarDato("codConcepto", Constantes.CONCEPTO_PP);
			datos.agregarDato("codOficina", datos.obtenerInteger("codOficinaRecuperado"));
			datos.agregarDato("codProducto", datos.obtenerInteger("codProductoRecuperado"));
			datos.agregarDato("numCuenta", datos.obtenerInteger("numCuentaRecuperado"));
			datos.agregarDato("digitoVerificador", datos.obtenerInteger("digitoVerificadorRecuperado"));
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			//fecha relativa recuperada de Seguridad Terminales Financieros
			//hora sistema recuperada de Seguridad Terminales Financieros
			datos.agregarDato("numTran", datos.obtenerInteger("numTran"));
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("codCompania", pcp.getCodCompania());
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("numCaja", peticion.getNumCaja());
			datos.agregarDato("montoIVA", null);
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codDebCre", datos.obtenerInteger("codDebCre"));
			datos.agregarDato("numSecuenciaCupon", null);
			datos.agregarDato("valorImpuestoVenta", null);
			datos.agregarDato("codSectorEconomico", codSectorEconomico);
			datos.agregarDato("numDiasAtras", null);
			//fechaSistema recuperada de Seguridad Terminales Financieros
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numDocumentoReversa", null);
			datos.agregarDato("saldoAnterior", null);
			datos.agregarDato("senAJATR", Constantes.NO);
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
			datos.agregarDato("senWANG", null);
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.SI);
			datos.agregarDato("senPosteo", Constantes.NO);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", BigDecimal.ONE);
			datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
			datos.agregarDato("valorCheque", null);
			datos.agregarDato("valorVenta", BigDecimal.ONE);
			datos.agregarDato("numDocumentoTran2", null);
			datos.agregarDato("valorChequesAjenos", null);
			datos.agregarDato("valorChequesExt", null);
			datos.agregarDato("valorChequesPropios", null);
			datos.agregarDato("descripcionTran", datos.obtenerString("descripcionTran"));
			datos.agregarDato("codBancoTransf", null);
			datos.agregarDato("numCuentaTransf", "0000000000000");
			datos.agregarDato("codPaisTransf", null);
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("codCliente", codCliente);
			datos.agregarDato("valorImpuesto", null);
			datos.agregarDato("tipDocumentoCliente", tipDocumentoCliente);
			datos.agregarDato("numDocumentoCliente", numDocumentoCliente);
			datos.agregarDato("numDocumentoImp", null);
			datos.agregarDato("codSubCausal", peticion.getCodSubcausal());
			registrarTransaccionAAATR(datos);
			
			
			logger.debug("Preparando para registrar eventos prestamo");
			logger.debug("Genera movimiento de liquidación en PPMTP");			
			
			datos.agregarDato("cuentaPrestamo", peticion.getCuentaTransaccion());
			datos.agregarDato("fechaSistema", datos.obtenerInteger("fechaSistemaAMD"));
			datos.agregarDato("codCausal", peticion.getCodCausal());
			//codPantalla recuperada de Seguridad Terminales Financieros
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codDestinoFondos", new Integer(0));
			datos.agregarDato("codOrigenFondos", new Integer(0));
			datos.agregarDato("codAccion", Constantes.ACCION_ADICION);
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("numResolucion", " ");
			datos.agregarDato("numActaResolucion", " ");
			datos.agregarDato("tasaAnualInteresAnticipado", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresCompensatorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresMoratorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresVencido", BigDecimal.ZERO);
			datos.agregarDato("codUsuarioActual", " ");
			datos.agregarDato("codUsuarioAnterior", " ");
			datos.agregarDato("codModificacion", Constantes.PS_MOD_PP_EFECTIVO_PRODUCE_SEGURO);
			datos.agregarDato("valorModificacion", peticion.getValorEfectivo().setScale(2, RoundingMode.HALF_UP).toString());
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("fechaReversa", new Integer(0));
			logger.debug("Registrando Evento Prestamo");
			registrarEventosPrestamo(datos);
			
			
			logger.debug("Preparando para generar nota bancaria de prestamo");
			datos.agregarDato("cuentaPrestamo", peticion.getCuentaTransaccion());
			datos.agregarDato("cuentaRelacionada", " ");
			//fechaSistemaAMD obtenido previamente
			//horaSistema recuperada de Seguridad Terminales Financieros
			datos.agregarDato("codCausal", peticion.getCodCausal());
			datos.agregarDato("codSubCausal", new Integer(0));
			//codPantalla recuperada de Seguridad Terminales Financieros
			datos.agregarDato("codConcepto", Constantes.CONCEPTO_PP);
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("codMonedaTran", peticion.getCodMoneda());
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codEstadoRegistro", Constantes.PP_COD_ESTADO_VIGENTE);
			datos.agregarDato("fechaInicialAfectaAnticipo", datos.obtenerInteger("fechaSistemaAMD"));
			datos.agregarDato("fechaFinalAfectaAnticipo", datos.obtenerInteger("fechaSistemaAMD"));
			datos.agregarDato("fechaInicialAfectaVencido", datos.obtenerInteger("fechaSistemaAMD"));
			datos.agregarDato("fechaFinalAfectaVencido", datos.obtenerInteger("fechaSistemaAMD"));
			datos.agregarDato("fechaVencimientoImpago", pcp.getFechaVencimientoPrestamo());
			datos.agregarDato("numFacturaFinalAfectada", new Integer(1));
			datos.agregarDato("numFacturaInicialAfectada", new Integer(1));
			datos.agregarDato("saldoTerceroAntesTran", BigDecimal.ZERO);
			datos.agregarDato("saldoTerceroDespuesTran", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresAnticipado", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresCompensatorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresMoratorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresVencido", BigDecimal.ZERO);
			datos.agregarDato("valorInteresAnticipado", BigDecimal.ZERO);
			datos.agregarDato("valorCapitalAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorMoraAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());
			datos.agregarDato("valorGastosAjenosFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorGastosPropiosFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorInteresCompensatorioFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorInteresVencidoAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorSeguroDanios", BigDecimal.ZERO);
			datos.agregarDato("valorOtrosSeguros", BigDecimal.ZERO);
			datos.agregarDato("valorSeguroVida", BigDecimal.ZERO);
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("senReversa", Constantes.NO);
			registrarNotasPrestamo(datos);
			
			logger.debug("Se evalúa si existe subsidio  por parte de BFA");
			if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("valorSubsidioBFA"), BigDecimal.ZERO)) {
				
				logger.debug("Se recupera del maestro de transacciones");
				Map<String, Object> map_DXMTR = jdbcTemplate.queryForMap(query(SELECT_SFBDB_DXMTR), Constantes.ISPEC_PP001);
				
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map_DXMTR);
				
				Integer codTransaccionSubsidio = adaptador.getInteger("codTransaccionSubsidio");
				Integer codDebcreSubsidio = adaptador.getInteger("codDebcreSubsidio");
				String codPantallaSubsidio = adaptador.getString("codPantallaSubsidio");

				logger.debug("Se definen variables a ser insertadas en la tabla de transacciones generales");
				
				Integer codCausalSubsidio = Constantes.PS_CAUSAL_SUBSIDIO_PRODUCE_SEGURO;
				
				logger.debug("Preparando datos para registrar en tabla AAATR");
				datos.agregarDato("codCausal", codCausalSubsidio);
				datos.agregarDato("codConcepto", Constantes.CONCEPTO_PP);
				datos.agregarDato("codOficina", datos.obtenerInteger("codOficinaRecuperado"));
				datos.agregarDato("codProducto", datos.obtenerInteger("codProductoRecuperado"));
				datos.agregarDato("numCuenta", datos.obtenerInteger("numCuentaRecuperado"));
				datos.agregarDato("digitoVerificador", datos.obtenerInteger("digitoVerificadorRecuperado"));
				datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
				datos.agregarDato("codTerminal", peticion.getCodTerminal());
				//fecha relativa recuperada de Seguridad Terminales Financieros
				//hora sistema recuperada de Seguridad Terminales Financieros
				datos.agregarDato("numTran", datos.obtenerInteger("numTran"));
				datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
				//codCompania recuperada de Seguridad Terminales Financieros
				datos.agregarDato("codMoneda", pcp.getCodMoneda());
				datos.agregarDato("numCaja", peticion.getNumCaja());
				datos.agregarDato("montoIVA", null);
				datos.agregarDato("codTran", codTransaccionSubsidio);
				datos.agregarDato("codCajero", peticion.getCodCajero());
				datos.agregarDato("codDebCre", codDebcreSubsidio);
				datos.agregarDato("numSecuenciaCupon", null);
				datos.agregarDato("valorImpuestoVenta", null);
				datos.agregarDato("codSectorEconomico", codSectorEconomico);
				datos.agregarDato("numDiasAtras", null);
				//fechaSistema recuperada de Seguridad Terminales Financieros
				datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
				datos.agregarDato("numDocumentoReversa", null);
				datos.agregarDato("saldoAnterior", null);
				datos.agregarDato("senAJATR", Constantes.NO);
				datos.agregarDato("senAutorizacion", Constantes.NO);
				datos.agregarDato("senReversa", Constantes.NO);
				datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
				datos.agregarDato("senWANG", null);
				datos.agregarDato("senDiaAnterior", Constantes.NO);
				datos.agregarDato("senImpCaja", Constantes.SI);
				datos.agregarDato("senPosteo", Constantes.NO);
				datos.agregarDato("valorAnterior", BigDecimal.ZERO);
				datos.agregarDato("valorCompra", BigDecimal.ONE);
				datos.agregarDato("valorMovimiento", datos.obtenerBigDecimal("valorSubsidioBFA"));
				datos.agregarDato("valorCheque", null);
				datos.agregarDato("valorVenta", BigDecimal.ONE);
				datos.agregarDato("numDocumentoTran2", null);
				datos.agregarDato("valorChequesAjenos", null);
				datos.agregarDato("valorChequesExt", null);
				datos.agregarDato("valorChequesPropios", null);
				datos.agregarDato("descripcionTran", datos.obtenerString("descripcionTran"));
				datos.agregarDato("codBancoTransf", null);
				datos.agregarDato("numCuentaTransf", "0000000000000");
				datos.agregarDato("codPaisTransf", null);
				datos.agregarDato("senACRM", Constantes.SI);
				datos.agregarDato("codCliente", codCliente);
				datos.agregarDato("valorImpuesto", null);
				datos.agregarDato("tipDocumentoCliente", tipDocumentoCliente);
				datos.agregarDato("numDocumentoCliente", numDocumentoCliente);
				datos.agregarDato("numDocumentoImp", null);
				datos.agregarDato("codSubCausal", peticion.getCodSubcausal());
				registrarTransaccionAAATR(datos);

				
				
				logger.debug("Preparando para registrar eventos prestamo");
				logger.debug("Genera movimiento de liquidación en PPMTP del subsidio");
				
				datos.agregarDato("cuentaPrestamo", peticion.getCuentaTransaccion());
				datos.agregarDato("fechaSistema", datos.obtenerInteger("fechaSistemaAMD"));
				datos.agregarDato("codCausal", codCausalSubsidio);
				datos.agregarDato("codPantalla", codPantallaSubsidio);
				datos.agregarDato("codCajero", peticion.getCodCajero());
				datos.agregarDato("codDestinoFondos", new Integer(0));
				datos.agregarDato("codOrigenFondos", new Integer(0));
				datos.agregarDato("codAccion", Constantes.ACCION_ADICION);
				datos.agregarDato("codMoneda", pcp.getCodMoneda());
				datos.agregarDato("numResolucion", " ");
				datos.agregarDato("numActaResolucion", " ");
				datos.agregarDato("tasaAnualInteresAnticipado", BigDecimal.ZERO);
				datos.agregarDato("tasaAnualInteresCompensatorio", BigDecimal.ZERO);
				datos.agregarDato("tasaAnualInteresMoratorio", BigDecimal.ZERO);
				datos.agregarDato("tasaAnualInteresVencido", BigDecimal.ZERO);
				datos.agregarDato("codUsuarioActual", " ");
				datos.agregarDato("codUsuarioAnterior", " ");
				datos.agregarDato("codModificacion", Constantes.PS_MOD_PP_EFECTIVO_PRODUCE_SEGURO);
				datos.agregarDato("valorModificacion", datos.obtenerBigDecimal("valorSubsidioBFA").toString());
				datos.agregarDato("senReversa", Constantes.NO);
				datos.agregarDato("fechaReversa", new Integer(0));
				logger.debug("Registrando Evento Prestamo");
				registrarEventosPrestamo(datos);
				
				
				logger.debug("Genera movimiento de liquidación en PPANB - subsidio");
				
				logger.debug("Preparando para generar nota bancaria de prestamo");
				datos.agregarDato("cuentaPrestamo", peticion.getCuentaTransaccion());
				datos.agregarDato("cuentaRelacionada", " ");
				//fechaSistemaAMD obtenido previamente
				//horaSistema recuperada de Seguridad Terminales Financieros
				datos.agregarDato("codCausal", codCausalSubsidio);
				datos.agregarDato("codSubCausal", new Integer(0));
				datos.agregarDato("codPantalla", codPantallaSubsidio);
				datos.agregarDato("codConcepto", Constantes.CONCEPTO_PP);
				datos.agregarDato("codMoneda", pcp.getCodMoneda());
				datos.agregarDato("codMonedaTran", peticion.getCodMoneda());
				datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
				datos.agregarDato("codEstadoRegistro", Constantes.PP_COD_ESTADO_VIGENTE);
				datos.agregarDato("fechaInicialAfectaAnticipo", datos.obtenerInteger("fechaSistemaAMD"));
				datos.agregarDato("fechaFinalAfectaAnticipo", datos.obtenerInteger("fechaSistemaAMD"));
				datos.agregarDato("fechaInicialAfectaVencido", datos.obtenerInteger("fechaSistemaAMD"));
				datos.agregarDato("fechaFinalAfectaVencido", datos.obtenerInteger("fechaSistemaAMD"));
				datos.agregarDato("fechaVencimientoImpago", pcp.getFechaVencimientoPrestamo());
				datos.agregarDato("numFacturaFinalAfectada", new Integer(1));
				datos.agregarDato("numFacturaInicialAfectada", new Integer(1));
				datos.agregarDato("saldoTerceroAntesTran", BigDecimal.ZERO);
				datos.agregarDato("saldoTerceroDespuesTran", BigDecimal.ZERO);
				datos.agregarDato("tasaAnualInteresAnticipado", BigDecimal.ZERO);
				datos.agregarDato("tasaAnualInteresCompensatorio", BigDecimal.ZERO);
				datos.agregarDato("tasaAnualInteresMoratorio", BigDecimal.ZERO);
				datos.agregarDato("tasaAnualInteresVencido", BigDecimal.ZERO);
				datos.agregarDato("valorInteresAnticipado", BigDecimal.ZERO);
				datos.agregarDato("valorCapitalAfectado", BigDecimal.ZERO);
				datos.agregarDato("valorMoraAfectada", BigDecimal.ZERO);
				datos.agregarDato("valorMovimiento", datos.obtenerBigDecimal("valorSubsidioBFA"));
				datos.agregarDato("valorGastosAjenosFacturados", BigDecimal.ZERO);
				datos.agregarDato("valorGastosPropiosFacturados", BigDecimal.ZERO);
				datos.agregarDato("valorInteresCompensatorioFacturados", BigDecimal.ZERO);
				datos.agregarDato("valorInteresVencidoAfectado", BigDecimal.ZERO);
				datos.agregarDato("valorSeguroDanios", BigDecimal.ZERO);
				datos.agregarDato("valorOtrosSeguros", BigDecimal.ZERO);
				datos.agregarDato("valorSeguroVida", BigDecimal.ZERO);
				datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
				datos.agregarDato("senReversa", Constantes.NO);
				registrarNotasPrestamo(datos);
			}
			
			logger.debug("Se verifican los gastos relacionados al préstamo y se obtiene el registro más reciente");
			
			Object[] paramsPPRGP = {
					datos.obtenerInteger("codOficinaRecuperado"),
					datos.obtenerInteger("codProductoRecuperado"),
					datos.obtenerInteger("numCuentaRecuperado"),
					Constantes.PS_COD_GASTO_SUBSIDIO_BFA
			};
			
			Long glbDtimeGastoBFA = jdbcTemplate.queryForObject(query(SELECT_SFBDB_PPRGP), Long.class, paramsPPRGP);	

			logger.debug("Se recupera el estado del gasto relacionado al registro recuperado");
			
			Integer codEstadoGasto = jdbcTemplate.queryForObject(query(SELECT_COD_ESTADO), Integer.class, glbDtimeGastoBFA);	
			
			if(UtileriaDeDatos.isEquals(codEstadoGasto, Constantes.PS_COD_ESTADO_GASTO_INGRESADO)) {
				
				Object[] params_PPRGP = {
						Constantes.PS_COD_ESTADO_GASTO_CANCELADO,
						glbDtimeGastoBFA
				};

				ejecutarSentencia(query(UPDATE_SFBDB_PPRGP), params_PPRGP);
			}

			logger.debug("Se evalúa el tipo  de seguro");
			
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("codTipoSeguro"), Constantes.PS_TIPO_SEGURO_PRODUCE)) {
				
				String tipoOperacionSeguro = Constantes.PS_SEGURO_INSCRIPCION;
				
				if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senRenovacion"), Constantes.SI)) {
					tipoOperacionSeguro = Constantes.PS_SEGURO_RENOVACION;
				}
				
				datos.agregarDato("codProducto", datos.obtenerInteger("codProductoRecuperado"));
				datos.agregarDato("codOficina", datos.obtenerInteger("codOficinaRecuperado"));
				datos.agregarDato("numCuenta", datos.obtenerInteger("numCuentaRecuperado"));
				datos.agregarDato("digitoVerificador", datos.obtenerInteger("digitoVerificadorRecuperado"));
				datos.agregarDato("codCliente", codCliente);
//				fechaSistemaAMD agregado en datos anteriormente
//				horaSistema recuperado de seguridad para terminales financieros
				datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
				datos.agregarDato("nomOficinaTran", datos.obtenerString("nomOficinaTran"));
				datos.agregarDato("codCajero", peticion.getCodCajero());
				datos.agregarDato("codTipoOperacion", tipoOperacionSeguro);
				
				logger.debug("Se invoca función para el registro o renovación de seguros");
				RegistrarInscripcionRenovacionProduceSeguro regRenovacionProduceSeguro = new RegistrarInscripcionRenovacionProduceSeguro(getJdbcTemplate(), getDbLinkValue());
				regRenovacionProduceSeguro.registrarInscripcionRenovacionSeguro(datos);
				
			}
			
			datos.agregarDato("fechaSistema", fechaSistema);
		}

		
	}
	
	
}
