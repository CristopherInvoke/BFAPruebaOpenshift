package sv.gob.bfa.deposito.mixto.cuentaahorro.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.model.CuentaAhorro;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.deposito.mixto.cuentaahorro.model.ReversaDepositoMixtoCuentaAhorroPeticion;
import sv.gob.bfa.deposito.mixto.cuentaahorro.model.ReversaDepositoMixtoCuentaAhorroRespuesta;

public class ReversaDepositoMixtoCuentaAhorroServicio extends Servicio{
	
	private final static String SELECT_NOMBRE_DOC_PERSONA_SIMP = 
			"SELECT ANO_CORTA as nombreDocumentoPerSimplifica" + 
			" FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			" WHERE ACO_TABLA = ?" + 
			"   AND ACO_CODIG = LPAD(?,2,0)";
	
	private final static String SELECT_SFBDB_BSRDC = 
			"SELECT COUNT(ACUNUMCUE)" + 
			" FROM LINC.SFBDB_BSRDC@DBLINK@" + 
			" WHERE ACO_CONCE = ?" + 
			"   AND SCOTIPDOC = ?" + 
			"   AND SNU_DOCUM = ?" + 
			"   AND ACU_OFICI = ?" + 
			"   AND ACU_PRODU = ?" + 
			"   AND ACUNUMCUE = ?";
	
	private final static String SELECT_SFBDB_DXMTR = 
			"SELECT DCO_TRANS as codTranDXMTR" + 
			" FROM LINC.SFBDB_DXMTR@DBLINK@"+ 
			" WHERE DCO_ISPEC = ?";
	
	private final static String SELECT_COUNT_SFBDB_AAATR = 
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
	
	private final static String SELECT_SFBDB_AAATR = 
			"SELECT TSEPOSTEO AS senPosteoAAATR," + 
			"       TVA_MOVIM AS valorMovimientoAAATR," + 
			"       GLB_DTIME as glbDtimeAAATR," + 
			"       ACO_CONCE as codConcepto" + 
			" FROM LINC.SFBDB_AAATR@DBLINK@" + 
			" WHERE TFETRAREL = ? " + 
			"   AND DCO_OFICI = ?" + 
			"   AND DCO_TERMI = ?" + 
			"   AND TNU_TRANS = ?" + 
			"   AND ACU_PRODU = ?" + 
			"   AND ACU_OFICI = ?" + 
			"   AND ACUNUMCUE = ?" + 
			"   AND ACUDIGVER = ?" + 
			"   AND TNUDOCTRA = ?" + 
			"   AND TSE_REVER != ?";
	
	private final static String UPDATE_SFBDB_AAATR = 
			"UPDATE LINC.SFBDB_AAATR@DBLINK@" + 
			" SET TSE_REVER = ?" + 
			" WHERE GLB_DTIME = ?"; 
	
	private final static String UPDATE_SFBDB_AAATR2 = 
			"UPDATE LINC.SFBDB_AAATR@DBLINK@" + 
			" SET ACO_CAUSA = ?" + 
			" WHERE GLB_DTIME = ?" ;
	
	private final static String UPDATE_SFBDB_AHMAH = 
			"UPDATE LINC.SFBDB_AHMAH@DBLINK@" + 
			" SET HSAEFEHOY = ?," +  
			"     HSAFLOH1  = ?," + 
			"     HSATOTHOY = ?," + 
			"     HFEULTTRA = ?," + 
			"     HCNLINDSI = ?," + 
			"     HSECERRAR = ?," + 
			"     HCOESTCUE = ?," + 
			"     HCNSINPOS = ?," + 
			"     HCN_TRANS = ?" + 
			" WHERE GLB_DTIME = ?";
	
	private final static String UPDATE_SFBDB_AHMAH2 = 
			"UPDATE LINC.SFBDB_AHMAH@DBLINK@" + 
			" SET HCNSINPOS = ?" + 
			" WHERE GLB_DTIME = ?" ;
	
	private final static String SELECT_FNC_CORREL_CANAL = 
			"SELECT MADMIN.FNC_CORREL_CANAL(?) as numTran FROM DUAL";
	

	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		
		logger.info("Iniciando servicio Deposito Mixto de cuenta ahorro");

		logger.debug("Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug("Cast de objeto de dominio -> ReversaDepositoMixtoCuentaAhorroPeticion ");
		ReversaDepositoMixtoCuentaAhorroPeticion peticion = (ReversaDepositoMixtoCuentaAhorroPeticion) objetoDom;
		
		try {
			
			logger.debug("Iniciando validaciones iniciales de parametros...");
			validacionParametrosIniciales(peticion);
			validacionCheques((ArrayList<Cheque>) peticion.getCheques());
			
			Integer codProductoCta = Integer.parseInt(peticion.getCuentaAhorro().substring(0, 3));
			datos.agregarDato("codProducto", codProductoCta);
			datos.agregarPropiedadesDeObjeto(peticion);
			
			logger.debug("Invocando la funcion de soporte 'Seguridad para Terminales financieros' ...");
			seguridadTerminalesFinancieros(datos);
			
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			datos.agregarDato("fechaUltimaTransaccion", fechaSistema);
			
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
			//Agregando nombre documento cliente para objeto respuesta
			datos.agregarDato("nombreDocumentoCliente", cliente.getNombreDocumentoCliente());
			
			//Definición de Código de Causal 
			datos.agregarDato("codCausal", peticion.getCodTran());
			
			logger.debug("Validación de la relación documento-cuenta");
			validarRelacionDocumentoCuenta(datos);
			
			logger.debug("Iniciando verificacion si se trata de una transacccion de apertura");
			verificacionTransaccionApertura(datos);
			
			logger.debug("Iniciando validacion de montos");
			validacionMonto(datos);
			
			logger.debug("Iniciando validacion de reversion de transaccion");
			validacionReversionTransaccion(datos);
			
			logger.debug("Iniciando transaccion en la tabla SFBDB_BSATA");
			registroTransaccionBSATA(datos);
			
			logger.debug("Actualizacion de saldos de cuentas de ahorro con montos de efectivos");
			actualizarSaldoCuenta(datos);
			
			logger.debug("Iniciando registro de la transaccion en el tanque de transacciones");
			registroTransaccionAAATR(datos);
			
			logger.debug("Iniciando proceso para lógica de pago de cheques propios y retenciones");
			ReversarChequesPropiosRetencionesGerencia(datos);
			
			logger.debug("Preparando respuesta a partir de objeto datosOperacion...");
			ReversaDepositoMixtoCuentaAhorroRespuesta respuesta = 
					new ReversaDepositoMixtoCuentaAhorroRespuesta();
			
			datos.llenarObjeto(respuesta);
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			logger.debug("Respuesta de Reversa Deposito Mixto AH (AH210) : " + respuesta);
			return respuesta;
		
		} catch (ServicioException e) {
			throw manejarMensajeExcepcionServicio(e);
		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
		
	}
	
	/**
	 * M&eacutetodo para validar parametros iniciales 
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionParametrosIniciales(ReversaDepositoMixtoCuentaAhorroPeticion peticion) throws ServicioException {
		
		
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumReversa(), "numReversa", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCuentaAhorro(), "cuentaAhorro", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaAhorro(), "cuentaAhorro", TipoValidacion.CADENA_NUMERICA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaAhorro(), "cuentaAhorro", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
		UtileriaDeParametros.validarParametro(peticion.getTipDocumentoPersona(), "tipDocumentoPersona", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoPersona(), "numDocumentoPersona", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNombrePersona(), "nombrePersona", TipoValidacion.CADENA_VACIA);
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
				UtileriaDeParametros.validarParametro(c.getTipCheque(), "tipCheque", TipoValidacion.ENTERO_VALOR_EN, new Integer[] {1,2,3,4,5});
				UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
				switch (c.getTipCheque()) {
				case 1:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: ", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla", TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal", TipoValidacion.ENTERO_MAYOR_CERO);

					break;
				case 2:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: ", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla", TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal", TipoValidacion.ENTERO_MAYOR_CERO);

					break;
				case 3:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCodBancoCheque(), "codBancoCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					
					break;
				case 4:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque" + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque" + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCodBancoCheque(), "codBancoCheque" + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque" + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					
					break;
				case 5:
					
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: ", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla", TipoValidacion.CADENA_VACIA);
					
					break;
				default:
					logger.error("Parametros no validos");
					throw new ServicioException(20001, "Parametros no validos");
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
			throw new ServicioException(20059, "Cuenta Inactiva. Avise al supervisor");
		}
		
		//Se verifica si es una cuenta electrónica.
		logger.debug("Se verifica si es una cuenta electrónica.");
		
		if(!UtileriaDeDatos.isEquals(codUsaLibreta, Constantes.AH_LIBRETA)) {
			senPosteo = Constantes.AH_SIMPLIFICADA;
		}
		
		//Agregando datos para funcion de soporte validarParametrosCuentaAhorro
		datos.agregarDato("senPosteo", senPosteo);
		datos.agregarDato("senCaja", Constantes.SI);
		datos.agregarDato("codDebCre", Constantes.DEBITO);
		datos.agregarDato("senReversa", Constantes.SI);
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
		String nombreDocumentoPersonaSimp = null;
		Integer contNumCuenta = null;
		
		ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca", CuentaAhorro.class);
			if(!UtileriaDeDatos.isBlank(peticion.getNumDocumentoPerSimplifica())) {
				
				try {
					logger.debug("Ejecutando sentencia SELECT NOMBRE DOC PERSONA SIMP...");
					nombreDocumentoPersonaSimp = jdbcTemplate.queryForObject(query(SELECT_NOMBRE_DOC_PERSONA_SIMP), 
							  String.class,"DOC-VIGFIN", peticion.getTipDocumentoPerSimplifica());
					
					if(UtileriaDeDatos.isNull(nombreDocumentoPersonaSimp)) {
						logger.debug("No existe documento vigente para realizar la transaccion");
						throw new ServicioException(20019, "No existe {}", "DOCUMENTO VIGENTE PARA REALIZAR LA TRANSACCION");
					}
					
				} catch (EmptyResultDataAccessException e) {
					logger.error("No existe documento vigente para realizar la transacción" + e.getMessage(), e);
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
				} catch (EmptyResultDataAccessException erdae) {
					logger.error("Relacion entre documento y cuenta no existe");
					throw new ServicioException(21261, "Relacion entre documento y cuenta no existe");
				}
				if(UtileriaDeDatos.isNull(contNumCuenta) || UtileriaDeDatos.isEquals(contNumCuenta, new Integer(0))) {
					logger.error("Relacion entre documento y cuenta no existe");
					throw new ServicioException(21261, "Relacion entre documento y cuenta no existe");
				}
				
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
			ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);

			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			Integer fechaRelativa = datos.obtenerInteger("fechaRelativa");
			Integer countCantidadMovimientos = null;

			if((UtileriaDeDatos.isEquals(pca.getFechaApertura(), fechaSistema)) && 
			   (UtileriaDeDatos.isEquals(pca.getFechaUltimaTran(), fechaSistema))) {
				
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
				
				logger.debug("Ejecutando sentencia SELECT COUNT SFBDB AAATR, parametros: " + Arrays.toString(paramsSFBDBAAATR));
				countCantidadMovimientos = jdbcTemplate.queryForObject(query(SELECT_COUNT_SFBDB_AAATR), Integer.class, paramsSFBDBAAATR);

				if(!UtileriaDeDatos.isNull(countCantidadMovimientos) && UtileriaDeDatos.isEquals(countCantidadMovimientos, new Integer(1))){
					
					Integer codTranDXMTR = jdbcTemplate.queryForObject(query(SELECT_SFBDB_DXMTR), Integer.class, Constantes.ISPEC_AH212);
					
					if(UtileriaDeDatos.isNull(codTranDXMTR) || UtileriaDeDatos.isEquals(codTranDXMTR, new Integer(0))) {
						logger.error("No existe la transaccion de apertura en DXMTR");
						throw new ServicioException(20019, "No existe {}", "LA TRANSACCION DE APERTURA EN DXMTR");
					}
					
					peticion.setCodTran(codTranDXMTR);
					datos.agregarDato("fechaUltimaTransaccion", new Integer(0));
					datos.agregarDato("codCausal", codTranDXMTR);
					datos.agregarDato("peticion", peticion);
				}
				
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

		ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);
			
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
				throw new ServicioException(20286, "Valor del movimiento no está cuadrado");
			}
			
			if(!UtileriaDeDatos.isEquals(sumCheques, peticion.getValorCheques())){
				throw new ServicioException(20286, "Valor del movimiento no está cuadrado");
			}
			
			if((UtileriaDeDatos.isGreater(peticion.getValorChequesPropios(), BigDecimal.ZERO) || 
					UtileriaDeDatos.isGreater(peticion.getValorChequesExt(), BigDecimal.ZERO) || 
					UtileriaDeDatos.isGreater(peticion.getValorChequesAjenos(), BigDecimal.ZERO)) 
					&& (UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques()) || UtileriaDeDatos.isEquals(peticion.getCheques().size(), 0))) {
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
				throw new ServicioException(21287, "Monto de cheques propios no está cuadrado");
			}
			
			if(!UtileriaDeDatos.isEquals(peticion.getValorChequesAjenos(), sumMontoChequesAjenos)) {
				throw new ServicioException(21288, "Monto de cheques ajenos no está cuadrado");
			}
			
			if(!UtileriaDeDatos.isEquals(peticion.getValorChequesExt(), sumMontoChequesExt)) {
				throw new ServicioException(21289, "Monto de cheques del exterior no está cuadrado");
			}
			
	}
	
	
	/**
	 * M&eacutetodo para validar que no se duplique el número de documento de la transacci&oacuten
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void validacionReversionTransaccion (DatosOperacion datos) throws ServicioException, TipoDatoException {
		
		Map<String, Object> queryForMap = null;
		Integer senPosteo = Constantes.NO;
		Integer senPosteoAAATR = 0;
		BigDecimal valorMovimientoAAATR = BigDecimal.ZERO;
		Integer codConcepto = 0;
		Long glbDtimeAAATR = 0l;
		Integer canMovimientosSinPostear = 0;

		CuentaAhorro pca = datos.obtenerObjeto("pca", CuentaAhorro.class);
		ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);
		Integer fechaRelativa = datos.obtenerInteger("fechaRelativa");
		
		try {
			
			Object[] paramsAAATR = {
					fechaRelativa,
					peticion.getCodOficinaTran(),
					peticion.getCodTerminal(),
					peticion.getNumReversa(),
					pca.getCodProducto(),
					pca.getCodOficina(),
					pca.getNumCuenta(),
					pca.getDigitoVerificador(),
					peticion.getNumDocumentoTran(),
					Constantes.SI
			}; 
			
			logger.debug("Ejecutando sentencia SELECT_SFBDB_AAATR, parametros: " + Arrays.toString(paramsAAATR));
			queryForMap = this.jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAATR), paramsAAATR);
			
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
			
			senPosteoAAATR = adaptador.getInteger("senPosteoAAATR");
			valorMovimientoAAATR = adaptador.getBigDecimal("valorMovimientoAAATR");
			glbDtimeAAATR = adaptador.getLong("glbDtimeAAATR");
			codConcepto = adaptador.getInteger("codConcepto");
			canMovimientosSinPostear = pca.getCanMovimientosSinPostear();
			
		} catch (EmptyResultDataAccessException erdae) {
		} 
		
			datos.agregarDato("glbDtime", glbDtimeAAATR);
			
			if(!UtileriaDeDatos.isEquals(valorMovimientoAAATR, peticion.getValorMovimiento())) {
				throw new ServicioException(20212, "Transacción no aparece en base de datos");
			}
			
			if(UtileriaDeDatos.isGreater(peticion.getValorMovimiento(), pca.getSaldoTotalHoy())) {
				throw new ServicioException(20273, "Fondos insuficientes");
			}
			
			logger.debug("Actualización de la transacción en la tabla LINC.SFBDB_AAATR");
			
			if(!UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.AH_LIBRETA)) {
				senPosteo = Constantes.AH_SIMPLIFICADA;
			}
			
			if(UtileriaDeDatos.isEquals(senPosteoAAATR, Constantes.NO) || UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.CC_CHEQUERA)) {
				
				Object[] paramsSFBDAAATR = {
						Constantes.SI,
						glbDtimeAAATR
				};
				
				ejecutarSentencia(query(UPDATE_SFBDB_AAATR), paramsSFBDAAATR);
				
				logger.debug("Se invoca la función de actualización de perfiles de transacción");
				
				datos.agregarDato("codTerminalTran", peticion.getCodTerminal());
				datos.agregarDato("numTran", peticion.getNumReversa());
				actualizarPerfilesTransaccionAAATR(datos);
				
				if(UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.AH_LIBRETA)) {
					pca.setCanMovimientosSinPostear(canMovimientosSinPostear-1);
				}else {
					pca.setCanMovimientosSinPostear(new Integer(0));
				}
			}
			
			datos.agregarDato("senPosteoAAATR", senPosteoAAATR);
			datos.agregarDato("senPosteo", senPosteo);
			datos.agregarDato("codConcepto", codConcepto);
			
		
	}
	
	/**
	 * M&eacutetodo para registrar la transacci&oacuten tabla SFBDB_BSATA
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void registroTransaccionBSATA (DatosOperacion datos) throws ServicioException, TipoDatoException {
		
			ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
			Integer senPosteoAAATR = datos.obtenerInteger("senPosteoAAATR");
			
			
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			datos.agregarDato("codGrupo", new Integer(0));
			datos.agregarDato("codCliente", cliente.getCodCliente());
			datos.agregarDato("numReversa", peticion.getNumReversa());
			datos.agregarDato("codOficina", pca.getCodOficina());
			datos.agregarDato("codProducto", pca.getCodProducto());
			datos.agregarDato("digitoVerificador", pca.getDigitoVerificador());
			datos.agregarDato("numCuenta", pca.getNumCuenta());
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			datos.agregarDato("valorCheques", peticion.getValorCheques());
			actualizarTransaccionBSATA(datos);
			
			if(UtileriaDeDatos.isEquals(senPosteoAAATR, Constantes.SI) && 
			   UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.AH_LIBRETA)) {
				
				Long glbDtimeAAATR = datos.obtenerLong("glbDtime");
				
				Object[] paramsSFBDAAATR = {
						Constantes.AH_REVERSA_DEPOSITO_ORIGINAL,
						glbDtimeAAATR
				};
				
				ejecutarSentencia(query(UPDATE_SFBDB_AAATR2), paramsSFBDAAATR);
			}

	}
	
	/**
	 * M&eacutetodo para  actualizar los valores de los saldos de la cuenta de ahorro con los montos de efectivos
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void actualizarSaldoCuenta (DatosOperacion datos) throws ServicioException, TipoDatoException {

			ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			Integer fechaSistema =  datos.obtenerInteger("fechaSistema");
			
			if(UtileriaDeDatos.isEquals(pca.getCodEstado(), Constantes.AH_ESTADO_INACTIVA)) {
				pca.setCodEstado(Constantes.AH_ESTADO_ACTIVA);
			}
			
			BigDecimal montoEfectivo = peticion.getValorEfectivo();
			montoEfectivo = montoEfectivo.add(peticion.getValorChequesPropios());
			BigDecimal montoCheques = peticion.getValorChequesAjenos();
			montoCheques = montoCheques.add(peticion.getValorChequesExt());
			
			logger.debug("Cálculo de valores a actualizar");

			BigDecimal saldoEfectivoHoy = pca.getSaldoEfectivoHoy();
			saldoEfectivoHoy = saldoEfectivoHoy.subtract(montoEfectivo);
			
			//BigDecimal saldoChequesHoy = pca.getSaldoChequeDiaHoy();
			//saldoChequesHoy = saldoChequesHoy.subtract(montoCheques);
			
			BigDecimal saldoFlotante = pca.getSaldoFlotante();
			saldoFlotante = saldoFlotante.subtract(montoCheques);
			
			BigDecimal saldoTotalHoy = pca.getSaldoTotalHoy();
			saldoTotalHoy = saldoTotalHoy.subtract(peticion.getValorMovimiento());
			
			Integer canLineasEstCtaDiaSiguiente = pca.getCanLineasEstCtaDiaSiguiente();
			canLineasEstCtaDiaSiguiente -=1;

			logger.debug("Se incrementa el número de transacciones acumuladas.");
			Integer canTransaccionesMes = pca.getCanTransaccionesMes();
			canTransaccionesMes +=1;
			
			pca.setSaldoEfectivoHoy(saldoEfectivoHoy);
			pca.setSaldoFlotante(saldoFlotante);
			//pca.setSaldoTotalHoy(saldoTotalHoy);
			pca.setCanLineasEstCtaDiaSiguiente(canLineasEstCtaDiaSiguiente);
			
			logger.debug("Actualización del maestro de cuentas de ahorro");
			
			pca.setCanTransaccionesMes(canTransaccionesMes+1);
			datos.agregarDato("pca", pca);
			
			Object[] paramsSFBDBAHMAH = {
					pca.getSaldoEfectivoHoy(),
					pca.getSaldoFlotante(),
					//pca.getSaldoTotalHoy(),
					saldoTotalHoy,
					datos.obtenerValor("fechaUltimaTransaccion"),
					pca.getCanLineasEstCtaDiaSiguiente(),
					Constantes.SI,
					pca.getCodEstado(),
					pca.getCanMovimientosSinPostear(),
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
		
			Integer senPosteoAAATR = datos.obtenerInteger("senPosteoAAATR");
			
			ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);
			Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
			
			CuentaAhorro pca = datos.obtenerObjeto("pca",CuentaAhorro.class);
			String descripcionTran = null;
			
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
			
			if(UtileriaDeDatos.isEquals(senPosteoAAATR, Constantes.SI) && 
			   UtileriaDeDatos.isEquals(pca.getCodUsoLibreta(), Constantes.AH_LIBRETA)) {

				descripcionTran =	"TIPO DOC.: " + peticion.getTipDocumentoPersona() +
									" NUM DOC.: "  + peticion.getNumDocumentoPersona() +
									" NOMBRE: "   + peticion.getNombrePersona();
			

			Integer numTran = jdbcTemplate.queryForObject(query(SELECT_FNC_CORREL_CANAL), Integer.class, Constantes.VENTANILLA);
			Integer codCausal = Constantes.AH_REVERSA_DEPOSITO;
			
			pca.setCanMovimientosSinPostear(pca.getCanMovimientosSinPostear()+1);
			
			Object[] paramsSFBDAHMAH = {
					pca.getCanMovimientosSinPostear(),
					pca.getGlbDtime()
			};
			
			ejecutarSentencia(query(UPDATE_SFBDB_AHMAH2), paramsSFBDAHMAH);
			
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("codOficina", pca.getCodOficina());
			datos.agregarDato("codProducto", pca.getCodProducto());
			datos.agregarDato("numCuenta", pca.getNumCuenta());
			datos.agregarDato("digitoVerificador", pca.getDigitoVerificador());
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("codMoneda", pca.getCodMoneda());
			datos.agregarDato("numCaja", peticion.getNumCaja());
			datos.agregarDato("montoIVA", null);
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("numSecuenciaCupon", null);
			datos.agregarDato("valorImpuestoVenta", null);
			datos.agregarDato("codSectorEconomico", cliente.getCodSectorEconomicoCliente());
			datos.agregarDato("numDiasAtras", null);
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numReversa", peticion.getNumReversa());
			datos.agregarDato("saldoAnterior", BigDecimal.ZERO);
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
			datos.agregarDato("codPaisTransf", null);
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("codCliente", cliente.getCodCliente());
			datos.agregarDato("valorImpuesto", BigDecimal.ZERO);
			datos.agregarDato("tipDocumentoCliente", peticion.getTipDocumentoPersona());
			datos.agregarDato("numDocumentoCliente", peticion.getNumDocumentoPersona());
			datos.agregarDato("numDocumentoImp",  new Integer(0));
			datos.agregarDato("codSubCausal",  new Integer(0));
			datos.agregarDato("horaTran",  datos.obtenerInteger("horaSistema"));
			
			registrarTransaccionAAATR(datos);
			
			}

	}
	
	
	/**
	 * M&eacutetodo para procesar l&oacutegica de pagos de cheques propios y retenciones
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void ReversarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {

		ReversaDepositoMixtoCuentaAhorroPeticion peticion = datos.obtenerObjeto("peticion", ReversaDepositoMixtoCuentaAhorroPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
		
		//Recuperando numTran del servicio en este paso para no perder valor,
		//porque se sustituye en FS Pagos cheques, y se setea al final
		Integer numTran = datos.obtenerInteger("numTran");
		Integer numDocumentoTran = datos.obtenerInteger("numDocumentoTran");

		if (!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques())) {
			for(Cheque chk: peticion.getCheques()) {
				if(UtileriaDeDatos.isEquals(chk.getTipCheque(), 1) || UtileriaDeDatos.isEquals(chk.getTipCheque(), 2)) {

					datos.agregarDato("codTran", chk.getCodTran());
					datos.agregarDato("codCausal", chk.getCodCausal());
					datos.agregarDato("numTran", chk.getNumTran());
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));
					
					reversaPagoCheques(datos);
					chk.setCodCajero(datos.obtenerString("codCajero"));
					chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
					chk.setNomOficina(datos.obtenerString("nomOficina"));

				}					
				else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 3) ||
						 UtileriaDeDatos.isEquals(chk.getTipCheque(), 4)){
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					datos.agregarDato("codBancoCheque", chk.getCodBancoCheque());
					datos.agregarDato("codPlazaCheque", chk.getCodPlazaCheque());
					datos.agregarDato("numOperInternacional", chk.getNumOperInternacional());
					datos.agregarDato("cuentaDestino", peticion.getCuentaAhorro());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));
					
					reversaRetenciones(datos);
					chk.setCodCajero(datos.obtenerString("codCajero"));
					chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
					chk.setNomOficina(datos.obtenerString("nomOficina"));
				}
				else {
					datos.agregarDato("codTran", chk.getCodTran());
					datos.agregarDato("codCausal", chk.getCodCausal());
					datos.agregarDato("numTran", chk.getNumTran());
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));
					
					reversaPagosChequesGerencia(datos);
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
		
		
		//Agregando propiedades de cliente para respuesta
		datos.agregarDato("numDocumentoCliente", cliente.getNumDocumentoCliente());
		datos.agregarDato("lugarExpedicion", cliente.getLugarExpedicion());
		datos.agregarDato("fechaExpedicion", cliente.getFechaExpedicion());
		datos.agregarDato("nombreDOcumentoCliente", cliente.getNombreDocumentoCliente());
		datos.agregarDato("nombreCompletoCliente", cliente.getNombreCompletoCliente());
		datos.agregarDato("codCliente", cliente.getCodCliente()); //cgonzalez 23/01/2021
		datos.agregarDato("numDocumentoCliente",cliente.getNumDocumentoCliente()); //cgonzalez 03/02/2021

	}

}
