package sv.gob.bfa.deposito.mixto.cuentaahorro.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.dao.EmptyResultDataAccessException;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.model.CuentaAhorro;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.deposito.mixto.cuentaahorro.model.DepositoMixtoCuentaAhorroPeticion;
import sv.gob.bfa.deposito.mixto.cuentaahorro.model.DepositoMixtoCuentaAhorroRespuesta;

public class DepositoMixtoCuentaAhorroServicio extends Servicio{
	
	private final static String SELECT_NOMBRE_DOC_PERSONA = 
			"SELECT ANO_CORTA as nombreDocumentoPersona" + 
			" FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			" WHERE ACO_TABLA = ?" + 
			" AND ACO_CODIG = LPAD(?,2,0)";

	private final static String SELECT_NOMBRE_DOC_PERSONA_SIMP = 
			"SELECT ANO_CORTA as nombreDocumentoPerSimplifica" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE ACO_TABLA = ?" + 
			"	AND ACO_CODIG = LPAD(?,2,0)";
	
	private final static String SELECT_SFBDB_BSRDC = 
			"SELECT COUNT(ACUNUMCUE)" + 
			"	FROM LINC.SFBDB_BSRDC@DBLINK@" + 
			"	WHERE ACO_CONCE = ?" + 
			"	AND SCOTIPDOC = ?" + 
			"	AND SNU_DOCUM = ?" + 
			"	AND ACU_OFICI = ?" + 
			"	AND ACU_PRODU = ?" + 
			"	AND ACUNUMCUE = ?";

	private final static String SELECT_SFBDB_DXMTR= 
			"SELECT DCO_TRANS as codTranDXMTR" + 
			" FROM LINC.SFBDB_DXMTR@DBLINK@"+ 
			" WHERE DCO_ISPEC = ?";
	
	private final static String SELECT_SFBDB_AAATR= 
			"SELECT COUNT(TNUDOCTRA) as cantidadMovimientos" + 
			" FROM LINC.SFBDB_AAATR@DBLINK@" + 
			" WHERE TFETRAREL = ?" + 
			"   AND DCO_OFICI = ?" + 
			"   AND DCO_TERMI = ?" + 
			"   AND ACU_OFICI = ?" + 
			"   AND ACU_PRODU = ?" + 
			"   AND ACUNUMCUE = ?" + 
			"   AND ACUDIGVER = ?" + 
			"   AND ACO_CAUSA != ?" + 
			"   AND ACO_CAUSA != ?" + 
			"   AND TSE_REVER != ?";
	
	private final static String SELECT_SFBDB_AAMPR = 
			"SELECT AMOMINAPE as montoMinimoApertura" + 
			" FROM LINC.SFBDB_AAMPR@DBLINK@" + 
			" WHERE ACO_CONCE = ?" + 
			"   AND ACO_PRODU = ?"; 
	
	private final static String SELECT_DOC_SFBDB_AAATR = 
			"SELECT COUNT(TNUDOCTRA)" + 
			" FROM LINC.SFBDB_AAATR@DBLINK@" + 
			" WHERE TFETRAREL = ?" + 
			"   AND ACO_CONCE = ?" + 
			"   AND ACU_OFICI = ?" + 
			"   AND ACU_PRODU = ?" + 
			"   AND ACUNUMCUE = ?" + 
			"   AND ACUDIGVER = ?" + 
			"   AND TNUDOCTRA = ?" + 
			"   AND ACO_CAUSA = ?" + 
			"   AND TSE_REVER != ?";
	
	private final static String UPDATE_SFBDB_AHMAH = 
			"UPDATE LINC.SFBDB_AHMAH@DBLINK@" + 
			" SET HSAEFEHOY = ?," + 
			"     HSAFLOH1  = ?," + 
			"     HSATOTHOY = ?," + 
			"     HFEULTTRA = ?," + 
			"     HCNLINHOY = ?," + 
//			"     HCNLINDSI = ?," + 
			" 	  HCNSINPOS = ?," +
			"     HSECERRAR = ?," + 
			"     HCOESTCUE = ?," + 
			"     HCN_TRANS = ?" + 
			" WHERE GLB_DTIME = ?";
	
	private final static String SELECT_FNC_CORREL_CANAL = 
			"SELECT MADMIN.FNC_CORREL_CANAL(?) as numTran FROM DUAL";
	
	private final static String SELECT_SFBDB_DAMOF = 
			"SELECT DVX_OFIAD AS codOfinaAdmin" + 
			" FROM LINC.SFBDB_DAMOF@DBLINK@" + 
			" WHERE DCO_OFICI = ?";
	
	private final static String UPDATE_SFBDB_AAARP = 
			"UPDATE LINC.SFBDB_AAARP@DBLINK@" + 
			"   SET ACO_CAUSA = ?, " + 
			"	ACO_CONCE = ?," + 
			"	ACU_OFICI = ?," + 
			"	ACU_PRODU = ?," + 
			"	ACUNUMCUE = ?," + 
			"	ACUDIGVER = ?," + 
			"	DCOTERADI = ?, " + 
			"	DCOTERUSO = ?," + 
			"	SCO_ESTAD = ?," + 
			"	SCOOFIUSO = ?," + 
			"	SCOUSUUSO = ?," + 
			"	SFE_USO   = ?," + 
			"	SHO_USO   = ?," + 
			"	TNUDOCTR2 = ?," + 
			"	TNUDOCTRA = ?," + 
			"	TVA_EFECT = ?," + 
			"	TVA_MOVIM = ?," + 
			"	TVA_VALOR = ?," + 
			"	TNU_TRANS = ?" + 
			"	WHERE SCOREGPRE = ?";
	
	
	
	
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		
		logger.info("Iniciando servicio Deposito Mixto de cuenta ahorro");

		logger.debug("Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug("Cast de objeto de dominio -> DepositoMixtoCuentaAhorroPeticion ");
		DepositoMixtoCuentaAhorroPeticion peticion = (DepositoMixtoCuentaAhorroPeticion) objetoDom;
		
		try {
			
			logger.debug("Iniciando validaciones iniciales de parametros...");
			validacionParametrosIniciales(peticion);
			validacionCheques((ArrayList<Cheque>) peticion.getCheques());
			
			Integer codProductoCta = Integer.parseInt(peticion.getCuentaAhorro().substring(0, 3));
			datos.agregarDato("codProducto", codProductoCta);
			datos.agregarPropiedadesDeObjeto(peticion);
			
			logger.debug("Invocando la funcion de soporte 'Seguridad para Terminales financieros' ...");
			seguridadTerminalesFinancieros(datos);
			
			logger.debug("Creando objeto auxiliar 'CuentaAhorro' con cuenta -> {} ", peticion.getCuentaAhorro());
			CuentaAhorro pca = recuperarDatosCuentaAhorro(peticion.getCuentaAhorro());
			
			logger.debug("Validacion de cuenta ahorro ...");
			validacionesCuentaAhorro(pca,datos);
			
			logger.debug("Se invoca la función de  soporte 'Validar Parametros Cuenta Ahorro'");
			validarParametrosCuentaAhorro(datos);
			
			logger.debug("Recuperacion de los datos del cliente ...");
			Cliente cliente = recuperarDatosCliente(pca.getCodCliente());
			
			//agregando propiedades del objeto al mapa 
			datos.agregarDato("peticion",peticion);
			datos.agregarDato("pca", pca);
			datos.agregarDato("cliente", cliente);
			//Agregando propiedad nombreDocumentoCliente
			datos.agregarDato("nombreDocumentoCliente", cliente.getNombreDocumentoCliente());
			
			//Definición de Código de Causal 
			datos.agregarDato("codCausal", peticion.getCodTran());
			
			logger.debug("Validación de la relación documento-cuenta");
			validarRelacionDocumentoCuenta(datos);
			
			logger.debug("Iniciando funcion de soporte 'Validar Registro UIF' (si aplica) ");
			validarRegistroUIF(datos);
			
			logger.debug("Iniciando verificacion si se trata de una transacccion de apertura");
			verificacionTransaccionApertura(datos);
			
			logger.debug("Iniciando verificacion que no se duplique el número de documento de la transaccion");
			validacionNoReversionTransaccion(datos);
			
			logger.debug("Iniciando validacion de montos");
			validacionMonto(datos);
			
			logger.debug("Iniciando validacion de participación de libreta válida");
			validarLibreta(datos);
			
			logger.debug("Iniciando actualizacion de valores de los saldos de cuenta de ahorro con los montos de efectivos");
			actualizarSaldoCuenta(datos);
			
			logger.debug("Iniciando registro de la transaccion en el tanque de transacciones");
			registroTransaccionAAATR(datos);
			
			logger.debug("Iniciando registro en la tabla SFBFB BSATR");
			registroTransaccionBSATR(datos);
			
			logger.debug("Iniciando actualización de registro de formulario de lavado de dinero UIF");
			actualizandoRegistroUIF(datos);
			
			logger.debug(" Iniciando proceso para lógica de pago de cheques propios y retenciones");
			procesarChequesPropiosRetencionesGerencia(datos);
			
			DepositoMixtoCuentaAhorroRespuesta respuesta = new DepositoMixtoCuentaAhorroRespuesta();
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			logger.debug("Respuesta de Servicio Deposito Mixto AH (AH210) : " + respuesta);
			return respuesta;
			
		} catch (ServicioException e) {
			throw manejarMensajeExcepcionServicio(e);
		} catch (TipoDatoException | ParseException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
	}
	
	
	/**
	 * M&eacutetodo para validar parametros iniciales 
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionParametrosIniciales(DepositoMixtoCuentaAhorroPeticion peticion) throws ServicioException {
		
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCuentaAhorro(), "cuentaAhorro", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaAhorro(), "cuentaAhorro", TipoValidacion.CADENA_NUMERICA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaAhorro(), "cuentaAhorro", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
		UtileriaDeParametros.validarParametro(peticion.getTipDocumentoPersona(), "tipDocumentoPersona", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoPersona(), "numDocumentoPersona", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNombrePersona(), "nombrePersona", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumTransLavado(), "numTransLavado", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorCheques(), "valorCheques", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);
		
		UtileriaDeParametros.validarParametro(peticion.getValorChequesPropios(), "valorChequesPropios", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorChequesAjenos(), "valorChequesAjenos", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorChequesExt(), "valorChequesExt", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
	}
	
	/**
	 * M&eacutetodo para validar el arreglo de cheques obtenidos de la peticion  
	 * @param  cheques
	 * @throws ServicioException
	 */
	private void validacionCheques(ArrayList<Cheque> cheques ) throws ServicioException {
		
		if(!UtileriaDeDatos.listIsEmptyOrNull(cheques)) {
			for (Cheque c : cheques) {
				
				Integer numCheque = c.getNumCheque();
				UtileriaDeParametros.validarParametro(c.getTipCheque(), "tipCheque del cheque " + numCheque, TipoValidacion.ENTERO_VALOR_EN, new Integer[] {1,2,3,4,5});
				UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);

				
				switch (c.getTipCheque()) {
				case 1:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getNumAutorizacion(), "numAutorizacion del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla del cheque " + numCheque, TipoValidacion.CADENA_VACIA);
					
					break;
				case 2:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getNumAutorizacion(), "numAutorizacion del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla del cheque " + numCheque, TipoValidacion.CADENA_VACIA);
					
					break;
				case 3:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: "+ numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCodBancoCheque(), "codBancoCheque del cheque: "+ numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: "+ numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPlazaCheque(), "codPlazaCheque del cheque: "+ numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					
					break;
				case 4:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: "+ numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: "+ numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCodBancoCheque(), "codBancoCheque del cheque:"+ numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: "+ numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getNumOperInternacional(), "numOperInternacional del cheque: "+ numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPlazaCheque(), "codPlazaCheque del cheque: "+ numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					
					break;
				case 5:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla del cheque " + numCheque, TipoValidacion.CADENA_VACIA);
					
					break;
				default:
					logger.error("Parametros no validos");
					throw new ServicioException(21010, "Parametros no validos", "tipCheque del numcheque: " + numCheque );
				}
			}
			
		}
		
		
	}
	
	/**
	 * M&eacutetodo encargado de realizar validaciones de datos recuperados de cuenta Ahorro
	 * @param pca
	 * @param datos
	 * @throws ServicioException
	 */
	private  void validacionesCuentaAhorro(CuentaAhorro pca, DatosOperacion datos) throws ServicioException{
		Integer estadoRecuperado = pca.getCodEstado();
		Integer codUsaLibreta = pca.getCodUsoLibreta();
		Integer senPosteo = Constantes.NO;
		
		//Validacion del estado de la cuenta
		logger.debug("Validacion del estado de la cuenta");
		if (UtileriaDeDatos.isEquals(estadoRecuperado, Constantes.AH_ESTADO_CERRADA) ||
			 UtileriaDeDatos.isEquals(estadoRecuperado, Constantes.AH_ESTADO_CANCELADA)) {
			logger.error("Cuenta Cerrada o Cancelada");
			throw new ServicioException(20056, "Cuenta Cerrada o Cancelada");
		}
		
		if (UtileriaDeDatos.isEquals(estadoRecuperado, Constantes.AH_ESTADO_PRECERRADA)) {
				logger.error("Cuenta ya fue pre-cerrada, no acepta movimientos");
				throw new ServicioException(20880, "Cuenta ya fue pre-cerrada, no acepta movimientos");
			}
		
		if (!UtileriaDeDatos.isEquals(estadoRecuperado, Constantes.AH_ESTADO_ACTIVA)) {
			logger.error("Cuenta Inactiva. Avise al supervisor");
			throw new ServicioException(20059, "CUENTA INACTIVA {} ", "- AVISE AL SUPERVISOR");
		}
		
		//Se verifica si la cuenta utiliza libreta o si es SIMPLIFICADA
		logger.debug("Se verifica si la cuenta utiliza libreta o si es SIMPLIFICADA");
		
		if(!UtileriaDeDatos.isEquals(codUsaLibreta, Constantes.AH_LIBRETA)) {
			senPosteo = Constantes.AH_SIMPLIFICADA;
		}
		
		datos.agregarDato("senPosteo", senPosteo);
		datos.agregarDato("senCaja", Constantes.SI);
		datos.agregarDato("codDebCre", Constantes.CREDITO);
		datos.agregarDato("senReversa", Constantes.NO);
		datos.agregarDato("codProducto", pca.getCodProducto());
		datos.agregarDato("codOficina", pca.getCodOficina());
		datos.agregarDato("numCuenta", pca.getNumCuenta());
		datos.agregarDato("digitoVerificador", pca.getDigitoVerificador());
		datos.agregarDato("senValidacionSaldos", Constantes.SI);
		
		
	}
	
	
	/**
	 * M&eacutetodo para validar relacion de documento con la cuenta
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void validarRelacionDocumentoCuenta(DatosOperacion datos) throws ServicioException, TipoDatoException {
		String nombreDocumentoPersona = null;
		String nombreDocumentoPersonaSimp = null;
		Integer contNumCuenta = null;
		try {
			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca", CuentaAhorro.class);
			
			logger.debug("Ejecutando sentencia SELECT NOMBRE DOC PERSONA");
			nombreDocumentoPersona = jdbcTemplate.queryForObject(query(SELECT_NOMBRE_DOC_PERSONA), String.class,"DOC-VIGFIN", peticion.getTipDocumentoPersona());
			datos.agregarDato("nombreDocumentoPersona", nombreDocumentoPersona);
			if(UtileriaDeDatos.isNull(nombreDocumentoPersona)) {
				logger.error("No existe documento vigente para realizar la transaccion");
				throw new ServicioException(20019, "No existe {}", "DOCUMENTO VIGENTE PARA REALIZAR LA TRANSACCION");
			}
			
			if(!UtileriaDeDatos.isBlank(peticion.getNumDocumentoPerSimplifica())) {

				try {
					logger.debug("Ejecutando sentencia SELECT NOMBRE DOC PERSONA SIMP...");
					nombreDocumentoPersonaSimp = jdbcTemplate.queryForObject(query(SELECT_NOMBRE_DOC_PERSONA_SIMP), 
							  String.class,"DOC-VIGFIN", peticion.getTipDocumentoPerSimplifica());
					
					datos.agregarDato("nombreDocumentoPersona", nombreDocumentoPersonaSimp);
					
				} catch (EmptyResultDataAccessException erdae) {
					logger.error("No existe documento vigente para realizar la transaccion");
					throw new ServicioException(20019, "No existe {}", "DOCUMENTO VIGENTE PARA REALIZAR LA TRANSACCION");
					
				}
				
				Object[] paramsSFBDBBSRDC = {
						Constantes.CONCEPTO_AH,
						peticion.getTipDocumentoPerSimplifica(),
						peticion.getNumDocumentoPerSimplifica(),
						pca.getCodOficina(),
						pca.getCodProducto(),
						pca.getNumCuenta()
					};
				
				try {
					logger.debug("Ejecutando sentencia SELECT SFBDB BSRDC, parametros: " + Arrays.toString(paramsSFBDBBSRDC));
					contNumCuenta = jdbcTemplate.queryForObject(query(SELECT_SFBDB_BSRDC), Integer.class, paramsSFBDBBSRDC);
				} catch (EmptyResultDataAccessException e) {
					logger.error("Relacion entre documento y cuenta no existe.");
					throw new ServicioException(21261, "Relacion entre documento y cuenta no existe.");
				}
				
				if(UtileriaDeDatos.isNull(contNumCuenta) || UtileriaDeDatos.isEquals(contNumCuenta, new Integer(0))) {
					logger.error("Relacion entre documento y cuenta no existe.");
					throw new ServicioException(21261, "Relacion entre documento y cuenta no existe.");
				}
			}
			
		}catch (EmptyResultDataAccessException erdae) {
			logger.error("No existe documento vigente para realizar la transaccion" + erdae.getMessage(), erdae);
			throw new ServicioException(20019, "No existe {}", "DOCUMENTO VIGENTE PARA REALIZAR LA TRANSACCION");
		}
	}
	
	/**
	 * M&eacutetodo para verificar si se trata de una transacci&oacuten de apertura
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void verificacionTransaccionApertura(DatosOperacion datos) throws ServicioException, TipoDatoException {
		
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);

			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			Integer fechaRelativa = datos.obtenerInteger("fechaRelativa");
			Integer codTranDXMTR = null;
			Integer cantidadMovimientos = null;
			Integer codProducto = pca.getCodProducto();
			boolean tranApertura = false;
			BigDecimal montoMinimoApertura = BigDecimal.ZERO;

			if((UtileriaDeDatos.isEquals(pca.getFechaApertura(), fechaSistema)) && 
			   (UtileriaDeDatos.isEquals(pca.getFechaUltimaTran(), new Integer(0)))) {

				try {
					codTranDXMTR = jdbcTemplate.queryForObject(query(SELECT_SFBDB_DXMTR), Integer.class,Constantes.ISPEC_AH212);
				} catch (EmptyResultDataAccessException erdae) {
					logger.error("No existe la transaccion de apertura en DXMTR.");
					throw new ServicioException(20019, "No existe {}", "LA TRANSACCION DE APERTURA EN DXMTR");
				}

				if(UtileriaDeDatos.isNull(codTranDXMTR)) {
					throw new ServicioException(20019, "No existe {}", "LA TRANSACCION DE APERTURA EN DXMTR");
				}else {
					tranApertura = true;
					peticion.setCodTran(codTranDXMTR);
					datos.agregarDato("codCausal", codTranDXMTR);
					datos.agregarDato("codTran", codTranDXMTR);
				}
			}
			
			logger.debug("Se valida que exista la transacción de apertura en la tabla de transacciones:");
			
			if( UtileriaDeDatos.isEquals(pca.getFechaApertura(), fechaSistema) &&
			    UtileriaDeDatos.isEquals(pca.getFechaUltimaTran(), fechaSistema)) {
				
				Object[] paramsSFBDBAAATR = {
						fechaRelativa,
						peticion.getCodOficinaTran(),
						peticion.getCodTerminal(),
						pca.getCodOficina(),
						pca.getCodProducto(),
						pca.getNumCuenta(),
						pca.getDigitoVerificador(),
						new Integer(800),
						new Integer(400),
						Constantes.SI
					};
				
				logger.debug("Ejecutando sentencia SELECT SFBDB AAATR, parametros: " + Arrays.toString(paramsSFBDBAAATR));
				cantidadMovimientos = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAATR), Integer.class, paramsSFBDBAAATR);	
				
				if(UtileriaDeDatos.isEquals(cantidadMovimientos, new Integer(0))) {
					logger.debug("Ejecutando sentencia SELECT SFBDB DXMTR...");
					codTranDXMTR = jdbcTemplate.queryForObject(query(SELECT_SFBDB_DXMTR), Integer.class,Constantes.ISPEC_AH212);
					if(UtileriaDeDatos.isNull(codTranDXMTR)) {
						logger.error("No existe la transaccion de apertura en DXMTR");
						throw new ServicioException(20019, "No existe {}", "LA TRANSACCION DE APERTURA EN DXMTR");
					}
					tranApertura = true;
					peticion.setCodTran(codTranDXMTR);
					datos.agregarDato("codCausal", codTranDXMTR);
					datos.agregarDato("peticion", peticion);
				}
			}
			
			//cgonzalez 20210119
			//Se atiende caso # 25789 - TAREA # 3449
			/*if(tranApertura == true) {
				try {
					logger.debug("Ejecutando sentencia SELECT SFBDB AAMPR...");
					montoMinimoApertura = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMPR), BigDecimal.class, 
							  Constantes.CONCEPTO_AH,codProducto);
				} catch (EmptyResultDataAccessException erdae) {
					logger.error("No existe registro Monto Mínimo Apertura");
					throw new ServicioException(20019, "No existe {}", "REGISTRO MONTO MINIMO APERTURA");
				}
				
				if(UtileriaDeDatos.isNull(montoMinimoApertura)) {
					logger.error("No existe registro Monto Mínimo Apertura");
					throw new ServicioException(20019, "No existe {}", "REGISTRO MONTO MINIMO APERTURA");
				}
				
				if(UtileriaDeDatos.isGreater(montoMinimoApertura, peticion.getValorMovimiento())) {
					logger.error("Monto minimo de apertura requerido para la operacion");
					throw new ServicioException(21020, "Monto minimo de apertura requerido para la operacion");
				}
				
			}*/			
	}
	
	/**
	 * M&eacutetodo para validar que no se duplique el número de documento de la transacci&oacuten
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void validacionNoReversionTransaccion (DatosOperacion datos) throws ServicioException, TipoDatoException {
		
		Integer resultado = null;
		try {
			
			CuentaAhorro pca = datos.obtenerObjeto("pca", CuentaAhorro.class);
			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			
			Object[] paramsAAATR = {
					datos.obtenerInteger("fechaRelativa"),
					Constantes.CONCEPTO_AH,
					pca.getCodOficina(),
					pca.getCodProducto(),
					pca.getNumCuenta(),
					pca.getDigitoVerificador(),
					peticion.getNumDocumentoTran(),
					datos.obtenerInteger("codCausal"),
					Constantes.SI
			};
			
			logger.debug("Ejecutando sentencia SELECT DOC SFBDB AAATR, parametros: " + Arrays.toString(paramsAAATR));
			resultado = jdbcTemplate.queryForObject(query(SELECT_DOC_SFBDB_AAATR),Integer.class,paramsAAATR);
			
			if(UtileriaDeDatos.isGreaterThanZero(resultado)) {
				logger.error("Documento ya existe registrado para la cuenta");
				throw new ServicioException(20004, "Documento ya existe registrado para la cuenta");
			}
		} catch (EmptyResultDataAccessException erdae) {
		} 
		
	}
	
	
	/**
	 * M&eacutetodo para verificar que el monto total del movimiento 
	 * coincide con la suma de los montos individuales de los valores declarados.
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void validacionMonto (DatosOperacion datos) throws ServicioException, TipoDatoException {

			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			
			BigDecimal sumMovimiento =  BigDecimal.ZERO;
			BigDecimal sumCheques = BigDecimal.ZERO;
			BigDecimal sumMontoChequesPropios = BigDecimal.ZERO;
			BigDecimal sumMontoChequesAjenos  = BigDecimal.ZERO;
			BigDecimal sumMontoChequesExt     = BigDecimal.ZERO;
			
			sumMovimiento = sumMovimiento.add(peticion.getValorEfectivo());
			sumMovimiento = sumMovimiento.add(peticion.getValorChequesPropios());
			sumMovimiento = sumMovimiento.add(peticion.getValorChequesAjenos());
			sumMovimiento = sumMovimiento.add(peticion.getValorChequesExt());
			
			sumCheques = sumCheques.add(peticion.getValorChequesPropios());
			sumCheques = sumCheques.add(peticion.getValorChequesAjenos());
			sumCheques = sumCheques.add(peticion.getValorChequesExt());
			
			if(!UtileriaDeDatos.isEquals(sumMovimiento, peticion.getValorMovimiento())) {
				logger.error("Valor del movimiento no esta cuadrado");
				throw new ServicioException(20286, "Valor del movimiento no esta cuadrado");
			}
			
			if(!UtileriaDeDatos.isEquals(sumCheques, peticion.getValorCheques())){
				logger.error("Valor del movimiento no esta cuadrado");
				throw new ServicioException(20286, "Valor del movimiento no esta cuadrado");
			}
			
			if((UtileriaDeDatos.isGreater(peticion.getValorChequesPropios(), BigDecimal.ZERO) || 
					UtileriaDeDatos.isGreater(peticion.getValorChequesExt(), BigDecimal.ZERO) || 
					UtileriaDeDatos.isGreater(peticion.getValorChequesAjenos(), BigDecimal.ZERO)) 
					&& (UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques()) || UtileriaDeDatos.isEquals(peticion.getCheques().size(), 0))) {
				logger.error("No se han recibido los datos de los cheques correspondientes al monto");
				throw new ServicioException(21286, "No se han recibido los datos de los cheques correspondientes al monto");
			}
			
			if (!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques())) {
				for (Cheque c : peticion.getCheques()) {
					switch (c.getTipCheque()) {
					case 1:
						sumMontoChequesPropios = sumMontoChequesPropios.add(c.getValorCheque());
						break;
					case 2:
						sumMontoChequesPropios = sumMontoChequesPropios.add(c.getValorCheque());
						break;
					case 3:
						sumMontoChequesAjenos = sumMontoChequesAjenos.add(c.getValorCheque());
						break;
					case 4:
						sumMontoChequesExt = sumMontoChequesExt.add(c.getValorCheque());
						break;
					case 5:
						sumMontoChequesPropios = sumMontoChequesPropios.add(c.getValorCheque());
						break;
					}
				}
			}
			
			if(!UtileriaDeDatos.isEquals(peticion.getValorChequesPropios(), sumMontoChequesPropios)) {
				logger.error("Monto de cheques propios no esta cuadrado");
				throw new ServicioException(21287, "Monto de cheques propios no esta cuadrado");
			}
			
			if(!UtileriaDeDatos.isEquals(peticion.getValorChequesAjenos(), sumMontoChequesAjenos)) {
				logger.error("Monto de cheques ajenos no esta cuadrado");
				throw new ServicioException(21288, "Monto de cheques ajenos no esta cuadrado");
			}
			
			if(!UtileriaDeDatos.isEquals(peticion.getValorChequesExt(), sumMontoChequesExt)) {
				logger.error("Monto de cheques del exterior no esta cuadrado");
				throw new ServicioException(21289, "Monto de cheques del exterior no esta cuadrado");
			}
			

	}
	
	private  void validarLibreta(DatosOperacion datos) throws ServicioException, TipoDatoException{
		
			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca", CuentaAhorro.class);
			
			if(UtileriaDeDatos.isEquals(peticion.getLibretaValida(), Constantes.SI) &&
			   UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.CC_CHEQUERA)) {
				logger.error("Cuenta no usa libreta");
				throw new ServicioException(21002, "Cuenta no usa libreta");
			}
			
			if(UtileriaDeDatos.isEquals(peticion.getLibretaValida(), Constantes.SI)){
				
				if(!UtileriaDeDatos.isEquals(peticion.getNumLibreta(), pca.getNumLibretaCuenta())) { 
					logger.error("Numero de libreta incorrecto");
					throw new ServicioException(20362, "Numero de libreta incorrecto");
				}
				
//				if(UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.CC_CHEQUERA)) {
//					logger.error("Chequera se utiliza solamente en cuenta corriente");
//					throw new ServicioException(20637, "Chequera se utiliza solamente en cuenta corriente");
//				}
				
				if(!UtileriaDeDatos.isEquals(peticion.getSaldoLibreta().abs(), pca.getSaldoUltimoPosteo().abs())) {
					logger.error("Saldo de libreta no coincide");
					throw new ServicioException(20638, "Saldo de libreta no coincide");
				}
			}
		
	}
	
	
	/**
	 * M&eacutetodo para  actualizar los valores de los saldos de la cuenta de ahorro con los montos de efectivos
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void actualizarSaldoCuenta (DatosOperacion datos) throws ServicioException, TipoDatoException {

			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			Integer canMovimientosSinPostear = pca.getCanMovimientosSinPostear();
			Integer senPosteo = datos.obtenerInteger("senPosteo");
			Integer fechaSistema =  datos.obtenerInteger("fechaSistema");
			
			if(UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.AH_LIBRETA)) {
				pca.setCanMovimientosSinPostear(canMovimientosSinPostear+1);
			}else {
				senPosteo = Constantes.AH_SIMPLIFICADA;
				pca.setCanMovimientosSinPostear(new Integer(0));
				datos.agregarDato("senPosteo", senPosteo);
			}
			
			logger.debug("Cálculo de valores a actualizar");

			BigDecimal montoEfectivo = peticion.getValorEfectivo();
			montoEfectivo = montoEfectivo.add(peticion.getValorChequesPropios());
			
			BigDecimal montoCheques = peticion.getValorChequesAjenos();
			montoCheques = montoCheques.add(peticion.getValorChequesExt());
			
			//Integer canLineasEstCtaDiaSiguiente = pca.getCanLineasEstCtaDiaSiguiente(); //cgonzalez
			//canLineasEstCtaDiaSiguiente +=1; //cgonzalez
			Integer canLineasEstCtaHoy = pca.getCanLineasEstCtaHoy(); //cgonzalez
			canLineasEstCtaHoy +=1;//cgonzalez

			Integer canTransaccionesMes = pca.getCanTransaccionesMes();
			canTransaccionesMes +=1;
			
			pca.setSaldoEfectivoHoy(pca.getSaldoEfectivoHoy().add(montoEfectivo));
			pca.setSaldoFlotante(pca.getSaldoFlotante().add(montoCheques));
			//pca.setSaldoTotalHoy(pca.getSaldoTotalHoy().add(peticion.getValorMovimiento()));
			BigDecimal saldoTotalHoy  = pca.getSaldoTotalHoy().add(peticion.getValorMovimiento());
			//pca.setCanLineasEstCtaDiaSiguiente(canLineasEstCtaDiaSiguiente); //cgonzalez
			pca.setCanLineasEstCtaHoy(canLineasEstCtaHoy);//cgonzalez
			
			if(UtileriaDeDatos.isEquals(pca.getCodEstado(), Constantes.AH_ESTADO_INACTIVA)) {
				pca.setCodEstado(Constantes.AH_ESTADO_ACTIVA);
			}
			
			logger.debug("Actualización del maestro de cuentas de ahorro");
			
			pca.setCanTransaccionesMes(canTransaccionesMes);
			datos.agregarDato("pca", pca);
			
			Object[] paramsSFBDBAHMAH = {
					pca.getSaldoEfectivoHoy(),
					pca.getSaldoFlotante(),
					//pca.getSaldoTotalHoy(),
					saldoTotalHoy,
					fechaSistema,
					//pca.getCanLineasEstCtaDiaSiguiente(),//cgonzalez
                    pca.getCanLineasEstCtaHoy(),//cgonzalez
					pca.getCanMovimientosSinPostear(),
					Constantes.SI,
					pca.getCodEstado(),
					pca.getCanTransaccionesMes(),
					pca.getGlbDtime()
			};
			
			logger.debug("saldoActualCuenta: " + saldoTotalHoy + " , saldoAnteriorCuenta: " + pca.getSaldoTotalHoy());
			datos.agregarDato("saldoActualCuenta", saldoTotalHoy);
			datos.agregarDato("saldoAnteriorCuenta", pca.getSaldoTotalHoy());
			
			logger.debug("Ejecutando sentencia UPDATE SFBDB AHMAH, parametros: " + Arrays.toString(paramsSFBDBAHMAH));
			ejecutarSentencia(query(UPDATE_SFBDB_AHMAH), paramsSFBDBAHMAH);

	}
	
	/**
	 * M&eacutetodo para registrar la transacci&oacuten en el tanque de transacciones
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void registroTransaccionAAATR (DatosOperacion datos) throws ServicioException, TipoDatoException {
		
			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
			Integer codSectorEconomico = cliente.getCodSectorEconomicoCliente();
			String codCliente = cliente.getCodCliente();
			Integer tipDocumentoCliente = cliente.getTipDocumentoCliente();
			String numDocumentoCliente = cliente.getNumDocumentoCliente();
			String nombreDOcumentoCliente= cliente.getNombreDocumentoCliente();
			
			//Agregando propiedades de cliente para respuesta
			datos.agregarDato("numDocumentoCliente", numDocumentoCliente);
			datos.agregarDato("tipDOcumentoCliente", tipDocumentoCliente);
			datos.agregarDato("nombreDOcumentoCliente", nombreDOcumentoCliente);
			datos.agregarDato("lugarExpedicion", cliente.getLugarExpedicion());
			datos.agregarDato("fechaExpedicion", cliente.getFechaExpedicion());

			String descripcionTran = 
					"TIPO DOC.: " + peticion.getTipDocumentoPersona() +
                    " NUM DOC.: "  + peticion.getNumDocumentoPersona() +
                    " NOMBRE: "   + peticion.getNombrePersona();

			logger.debug("Ejecutando sentencia SELECT FNC CORREL CANAL...");
			Integer numTran = jdbcTemplate.queryForObject(query(SELECT_FNC_CORREL_CANAL), Integer.class, Constantes.VENTANILLA);
			
			Integer horaSistema = datos.obtenerInteger("horaSistema");

			datos.agregarDato("codBancoTransf", peticion.getNumTarjeta());
			if (UtileriaDeDatos.isBlank(peticion.getNumTarjeta())) {
				datos.agregarDato("codBancoTransf", null);
			}
			datos.agregarDato("codConcepto", Constantes.CONCEPTO_AH);
			datos.agregarDato("codOficina", pca.getCodOficina());
			datos.agregarDato("codProducto", pca.getCodProducto());
			datos.agregarDato("numCuenta", pca.getNumCuenta());
			datos.agregarDato("digitoVerificador", pca.getDigitoVerificador());
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			datos.agregarDato("horaTran", horaSistema);
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("codMoneda", pca.getCodMoneda());
			datos.agregarDato("numCaja", peticion.getNumCaja());
			datos.agregarDato("montoIVA", null);
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("numSecuenciaCupon", null);
			datos.agregarDato("valorImpuestoVenta", null);
			datos.agregarDato("codSectorEconomico", cliente.getCodSectorEconomicoCliente());
			datos.agregarDato("numDiasAtras", null);
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numDocumentoReversa", new Integer(0));
			datos.agregarDato("saldoAnterior", pca.getSaldoTotalHoy());
			datos.agregarDato("senAJATR", Constantes.NO);
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
			datos.agregarDato("senWANG", new Integer(0));
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.NO);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", BigDecimal.ONE);
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			datos.agregarDato("valorCheque", peticion.getValorCheques());
			datos.agregarDato("valorVenta", BigDecimal.ONE);
			datos.agregarDato("numDocumentoTran2", new Integer(0));
			datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());
			datos.agregarDato("valorChequesExt", peticion.getValorChequesExt());
			datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
			datos.agregarDato("descripcionTran", descripcionTran);
			datos.agregarDato("numCuentaTransf", "0000000000000");
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("codCliente", cliente.getCodCliente());
			datos.agregarDato("valorImpuesto", BigDecimal.ZERO);
			datos.agregarDato("tipDocumentoCliente", tipDocumentoCliente);
			datos.agregarDato("numDocumentoCliente", numDocumentoCliente);
			datos.agregarDato("numDocumentoImp",  new Integer(0));
			datos.agregarDato("codSubCausal",  new Integer(0));
			registrarTransaccionAAATR(datos);	
			
			datos.agregarDato("nomCuenta", pca.getNomCuenta());	
	}
	
	
	/**
	 * M&eacutetodo para registrar en la tabla SFBDB_BSATR
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void registroTransaccionBSATR (DatosOperacion datos) throws ServicioException, TipoDatoException {
		
			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
			
			logger.debug("Ejecutando sentencia SELECT SFBDB DAMOF...");
			Integer codOficinaAdmin = jdbcTemplate.queryForObject(query(SELECT_SFBDB_DAMOF), Integer.class, peticion.getCodOficinaTran());
			
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			datos.agregarDato("codGrupo", new Integer(0));
			datos.agregarDato("codCliente", cliente.getCodCliente());
			datos.agregarDato("codOficina", pca.getCodOficina());
			datos.agregarDato("codProducto", pca.getCodProducto());
			datos.agregarDato("digitoVerificador", pca.getDigitoVerificador());
			datos.agregarDato("numCuenta", pca.getNumCuenta());
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			datos.agregarDato("valorCheque", peticion.getValorCheques());
			datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());
			datos.agregarDato("valorChequesExt", peticion.getValorChequesExt());
			datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
			datos.agregarDato("codOficinaAdmin", codOficinaAdmin);
			datos.agregarDato("valorImpuesto", BigDecimal.ZERO);
			datos.agregarDato("codMontoExento", new Integer(0));
			datos.agregarDato("numDocumentoImp", new Integer(0));
			datos.agregarDato("saldoAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorSujetoImp", BigDecimal.ZERO);
			datos.agregarDato("codSubCausal", new Integer(0));
			registrarTransaccionBSATR(datos);

	}
	
	/**
	 * M&eacutetodo para actualizar registros de formulario de lavado de dinero UIF
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void actualizandoRegistroUIF(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException{
		
			DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			Integer fechaSistemaAMD = UtileriaDeDatos.toFecha8(UtileriaDeDatos.fecha6ToDate(fechaSistema));
			
			if(UtileriaDeDatos.isGreater(peticion.getNumTransLavado(), 0) ) {
				
				Object[] paramsSFBDBAAARP = {
						datos.obtenerInteger("codCausal"),
						datos.obtenerInteger("codConcepto"),
						pca.getCodOficina(),
						pca.getCodProducto(),
						pca.getNumCuenta(),
						pca.getDigitoVerificador(),
						datos.obtenerInteger("rpCodTerminalReg"),
						peticion.getCodTerminal(),
						Constantes.UIF_ESTADO_USADO,
						peticion.getCodOficinaTran(),
						peticion.getCodCajero(),
						fechaSistemaAMD,
						datos.obtenerInteger("horaSistema"),
						new Integer(0),
						peticion.getNumDocumentoTran(),
						peticion.getValorEfectivo(),
						peticion.getValorMovimiento(),
						peticion.getValorCheques(),
						datos.obtenerInteger("numTran"),
						peticion.getNumTransLavado()
				};
				
				logger.debug("Ejecutando sentencia UPDATE SFBDB AAARP, parametros: " + Arrays.toString(paramsSFBDBAAARP));
				ejecutarSentencia(query(UPDATE_SFBDB_AAARP), paramsSFBDBAAARP);
				
			}
			
			
			
	}
	
	/**
	 * M&eacutetodo para procesar l&oacutegica de pagos de cheques propios y retenciones
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void procesarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {

		DepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", DepositoMixtoCuentaAhorroPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
		CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
		
		
		//Recuperando numTran del servicio en este paso para no perder valor,
		//porque se sustituye en FS Pagos cheques, y se setea al final
		Integer numTran = datos.obtenerInteger("numTran");
		Integer numDocumentoTran = datos.obtenerInteger("numDocumentoTran");
		String codPantalla = "";
		if (!UtileriaDeDatos.isNull(datos.obtenerValor("codPantalla"))) {
			codPantalla	= datos.obtenerString("codPantalla");
		}

		if (!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques()) ) {
			for(Cheque chk: peticion.getCheques()) {
				if(UtileriaDeDatos.isEquals(chk.getTipCheque(), 1) ||
						UtileriaDeDatos.isEquals(chk.getTipCheque(), 2)) {
					datos.agregarDato("codTran", chk.getCodTran());
					datos.agregarDato("codCausal", chk.getCodCausal());
					datos.agregarDato("codPantalla", chk.getCodPantalla());
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					datos.agregarDato("autMaxDiasCheque", chk.getAutMaxDiasCheque());
					datos.agregarDato("numAutorizacion", chk.getNumAutorizacion());
					
					datos.agregarDato("tipDocumentoPersona", peticion.getTipDocumentoPersona());
					datos.agregarDato("numDocumentoPersona", peticion.getNumDocumentoPersona());
					datos.agregarDato("nombrePersona", peticion.getNombrePersona());
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));
					datos.agregarDato("senSupervisor", peticion.getSenSupervisor());

					pagosCheques(datos);
					chk.setNumTran(datos.obtenerInteger("numTran"));
					chk.setCodCajero(datos.obtenerString("codCajero"));
					chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
					chk.setNomOficina(datos.obtenerString("nomOficina"));
				}
				else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 3) ||
						 UtileriaDeDatos.isEquals(chk.getTipCheque(), 4)){
					datos.agregarDato("cuentaDestino", peticion.getCuentaAhorro());
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					datos.agregarDato("codBancoCheque", chk.getCodBancoCheque());
					datos.agregarDato("codPlazaCheque", chk.getCodPlazaCheque());
					datos.agregarDato("numOperInternacional", chk.getNumOperInternacional());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));
					
					retenciones(datos);
					chk.setCodCajero(datos.obtenerString("codCajero"));
					chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
					chk.setNomOficina(datos.obtenerString("nomOficina"));
				}
				else{
					logger.info("[17.3 Procesar cheques de Gerencia]");
					logger.debug("cheque gerencia " + peticion.getCheques());
					
					datos.agregarDato("codTran", chk.getCodTran());
					datos.agregarDato("codCausal", chk.getCodCausal());
					datos.agregarDato("codPantalla", chk.getCodPantalla());
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));
					datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
					
					datos.agregarDato("tipDocumentoPersona", peticion.getTipDocumentoPersona());
					datos.agregarDato("numDocumentoPersona", peticion.getNumDocumentoPersona());
					datos.agregarDato("nombrePersona", peticion.getNombrePersona());
					
					logger.debug("valorCheque" + chk.getValorCheque());
					
					pagosChequesGerencia(datos);
					chk.setNumTran(datos.obtenerInteger("numTran"));
					chk.setCodCajero(datos.obtenerString("codCajero"));
					chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
					chk.setNomOficina(datos.obtenerString("nomOficina"));
				}
				
				chk.setFechaReal(datos.obtenerInteger("fechaReal"));
				chk.setFechaRelativa(datos.obtenerInteger("fechaRelativa"));
				chk.setFechaSistema(datos.obtenerInteger("fechaSistema"));
				chk.setHoraSistema(datos.obtenerInteger("horaSistema"));
			}
			datos.agregarDato("cheques", peticion.getCheques());
		}

		datos.agregarDato("valorCheques", peticion.getValorCheques());
		datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());
		datos.agregarDato("valorChequesExt", peticion.getValorChequesExt());
		datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
		datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
		datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());

		datos.agregarDato("numTran", numTran);
		datos.agregarDato("numDocumentoTran", numDocumentoTran);
		datos.agregarDato("codPantalla", codPantalla);
		datos.agregarDato("codCliente", cliente.getCodCliente());	//cgonzalez 23/01/2021	
		datos.agregarDato("numDocumentoCliente",cliente.getNumDocumentoCliente()); //cgonzalez 03/02/2021	
	}
	

}
