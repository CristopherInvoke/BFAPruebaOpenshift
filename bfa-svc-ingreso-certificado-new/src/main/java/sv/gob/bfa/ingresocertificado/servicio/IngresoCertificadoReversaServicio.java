package sv.gob.bfa.ingresocertificado.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import sv.gob.bfa.core.fs.FSRecuperarDatosComprobante;
import sv.gob.bfa.core.model.Certificado;
import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.model.Comprobante;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.ingresocertificado.model.IngresoCertificadoReversaPeticion;
import sv.gob.bfa.ingresocertificado.model.IngresoCertificadoReversaRespuesta;

/**
 * @author Claudia Gonzalez
 * @version 27/11/2019
 * La lógica para la reversa del ingreso de Certificado por Caja (FF200), se ha creado a partir del documento Disenio_Reversa_Ingreso_Certificado_Caja_FF200
 * Permite realizar la reversa del ingreso de certificado por caja, considerando que es una transacción que requeire supervisor y que se haya realizado el mismo día. 
 * Se crea un objeto principal llamado "datos", el cual se va llenando con todas las propiedades y objetos recuperados de cada actividad
 * o validación realizada, para ello se utilizan 2 funciones principales, de acuerdo a la necesidad: agregarDato, agregarPropiedades.
 * Se crea el objeto "peticion" El cual se crea a partir de la clase IngresoCertificadoReversaPeticion, que son los campos que llenará la aplicación cliente.
 * Se crea el objeto "comprobante": El cual se crea partir de la función de soporte FSRecuperarDatosComprobante ubicada en svc-comunes 
 * Se crea el objeto "certificado": El cual se crea partir de la función de soporte FSRecuperarDatosCuentaCertificado ubicada en svc-comunes 
 * Se crea el objeto "respuesta":El cual se crea a partir de la clase IngresoCertificadoReversaRespuesta, que son los campos que se enviarán a la aplicación cliente
 * Así mismo se declaran como constantes todas las sentencias SQL a ser consumidas tanto para CONSULTAS como para las ACTUALIZACIONES.
 * Se consumen métodos definidos en la clase Servicio (Ubicada en Comunes), como por ejemplo la ejecución de las consultas y actualizaciones 
 * */

/**
 * CLASE CORRESPONDIENTE IngresoCertificadoReversa
 */

public class IngresoCertificadoReversaServicio extends Servicio{
	
	//SENTENCIAS A CONSUMIR EN LA LÓGICA
	private final static String SELECT_SFBDB_AAMPR_PRODUCTOS_V4 = ""
			+ "	SELECT fse_vista AS senProductoVista, "
			+ "        anucuepro AS cantidadCuentas "
			+ "	  FROM linc.sfbdb_aampr@DBLINK@"
			+ "  WHERE aco_produ = ?"
			;
	
	private final static String SELECT_SFBDB_DAMOF_ZONA_ORIGEN_V8_1 = " "
			+ " SELECT dco_zona AS codZona " 
			+ "   FROM linc.sfbdb_damof@DBLINK@ " 
			+ "  WHERE dco_ofici =  ? " 
			;
	
	private final static String SELECT_SFBDB_DAMOF_ZONA_TRANS_V8_2 = " "
			+ " SELECT dco_zona AS codZonaTran ,"
			+ "        dno_ofici AS nomOficina "
			+ "   FROM linc.sfbdb_damof@DBLINK@    "
			+ "  WHERE dco_ofici = ?" 
			;
		
	private final static String SELECT_SFBDB_AAATR_V10_1 =""
			+ "SELECT glb_dtime as glbDtimeAAATR"
			+ "  FROM linc.sfbdb_aaatr@DBLINK@"
			+ " WHERE tfetrarel  = ?"
			+ "   AND dco_ofici  = ?"
			+ "   AND dco_termi  = ?"
			+ "   AND tnu_trans  = ?"
			+ "   AND dco_trans  = ?"
			+ "   AND acu_produ  = ?"
			+ "   AND acu_ofici  = ?"
			+ "   AND acunumcue  = ?"
			+ "   AND acudigver  = ?"
			+ "   AND tnudoctra  = ?"
			+ "   AND tva_movim  = ?"
			+ "   AND tse_rever  <> ?"
			;
	
	private final static String UPDATE_SFBDB_AAATR_V10_2 = ""
			+ " UPDATE linc.sfbdb_aaatr@DBLINK@  "
			+ "	   SET tse_rever = ? "
			+ "  WHERE glb_dtime = ? "
			;
	
	private final static String UPDATE_SFBDB_FFACO_V11_1 = ""
			+" UPDATE linc.sfbdb_ffaco@DBLINK@ "
			+"    SET fcoestcom = ?," 
			+"        fco_cajer = ?,"
			+"        fho_cajer = ? "
			+"  WHERE fco_tipco = ? " 
			+"    AND fcoofiter = ? " 
			+"    AND fcu_produ = ? "
			+"    AND fnu_compr = ? "
			;
	
	private final static String UPDATE_SFBDB_FFMDE_V11_2= ""
			+" UPDATE linc.sfbdb_ffmde@DBLINK@ "
			+"    SET fco_estde = ?,"
			+"        ffeulttra = ? "
			+"  WHERE fcu_produ = ? "
			+"    AND fcu_ofici = ? "
			+"    AND fcunumcue = ? "
			;
	
	private final static String UPDATE_SFBDB_AAMPR_V11_3 = ""
			+" UPDATE linc.sfbdb_aampr@DBLINK@ "
			+"    SET anucuepro = ?"
			+"  WHERE aco_produ = ? "
			;
	
	Logger logger = LoggerFactory.getLogger(IngresoCertificadoReversaServicio.class);

	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * METODO PRINCIPAL, CONTIENE TODA LA LOGICA DE NEGOCIO
	 */
	@Transactional("transactionManager")
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		logger.info("Iniciando servicio IngresoCertificadoReversa");
		
		logger.debug("Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug("Cast de objeto de dominio -> IngresoCertificadoReversaPeticion ");
		IngresoCertificadoReversaPeticion peticion = (IngresoCertificadoReversaPeticion) objetoDom;
		
		//Agregando objeto "peticion" y sus propiedades al objeto "datos"
		datos.agregarDato("peticion", peticion);
		datos.agregarPropiedadesDeObjeto(peticion);
		datos.agregarDato("codProducto",peticion.getCodProductoAux());
		
		try {		
			
			// 1. Validar Parámetros
			// VALIDACION No. 1
			validacionInicial(peticion);		
			
			// VALIDACION No. 2
			// 02. Validar Parámetros
			validacionCheques((ArrayList<Cheque>) peticion.getCheques());
			
			// VALIDACION No. 3
			//3. Invocar Función Soporte Seguridad		
			validarSeguridadTerminalesFinancieros(datos);		
			
			// VALIDACION No. 4			
			//4. Verificar producto
			verificarProducto(datos);
	
			// VALIDACION No. 5		
			//5. Recuperar datos del comprobante
			recuperarValidarDatosComprobante(datos);
			
			// VALIDACION No. 6		
			//6. Recuperar datos del certificado en FFMDE
			recuperarValidarDatosCuentaCertificado(datos);
			
			// VALIDACION No. 7 
			//7. Recuperar Datos del Cliente
			recuperarValidarDatosCliente(datos);
			
			// VALIDACION No. 8		
			//8. Validar Oficinas
			validarOficinas(datos);
			
			// VALIDACION No. 9
			//9. Validar datos del Certificado de Depósito
			validarCertificadoDeposito(datos);
			
			// VALIDACION No. 10
			//10. Actualizar Datos de transacción en AAATR
			actualizarTransaccionAAATR(datos);
			
			// VALIDACION No. 11
			//11. Registrar Transacción en tabla AAATR			
			ReversarChequesPropiosRetencionesGerencia(datos);
			
			// VALIDACION No. 12
			//12. Actualizar Datos de Comprobante, Certificado y Producto
			actualizarDatosComprobanteCertificadoPorducto(datos);
	
			logger.debug("Preparando objeto de respuesta ...");
			
			IngresoCertificadoReversaRespuesta respuesta = new IngresoCertificadoReversaRespuesta(); 
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("Exito");
			
			if(logger.isDebugEnabled()) {
				logger.debug("RESPUESTA IngresoCertificadoReversa : {} ", respuesta);
			}
			
			return respuesta;
		
		} catch (ServicioException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(e);
		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 1
	/**
	 * Método auxiliar para validar peticion recibida
	 * @param peticion
	 * @throws ServicioException
	 */
	private void validacionInicial(IngresoCertificadoReversaPeticion peticion)  throws ServicioException{
		logger.info("[01. Validar Parámetros]");
		logger.debug("Peticion recibida: {}", peticion);	

		//1. ● codProductoAux > 0
		UtileriaDeParametros.validarParametro(peticion.getCodProductoAux(), "codProductoAux", TipoValidacion.ENTERO_MAYOR_CERO);		
		
		//2. ● numDocumentoTran > 0
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);			

		//3. ● valorEfectivo >= 0.00
		UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);			

		//4. ● valorCheques >= 0.00
		UtileriaDeParametros.validarParametro(peticion.getValorCheques(), "valorCheques", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);			

		//5. ● valorMovimiento > 0
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);			

		//6. ● codTran > 0
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);

		//7. ● senSupervisor	in (1,2)
		if (!UtileriaDeDatos.estaIncluidoEn(peticion.getSenSupervisor(), new Integer[] { 1, 2 })) {
			throw new ServicioException(21010, "Señal del supervisor debe ser 1 o 2", "señal Supervisor");
		}			

		//8.  ● numCaja > 0		
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);			

		//9. ● codCajero diferente vacío o espacios
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);			

		//10. ● codTerminal > 0
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);			
		
		//11. ●  codOficinaTran > 0
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//12. ●  numReversa > 0
		UtileriaDeParametros.validarParametro(peticion.getNumReversa(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//13. ● valorChequesPropios >= 0
		UtileriaDeParametros.validarParametro(peticion.getValorChequesPropios(), "valorChequesPropios", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
				
		//14. ● valorChequesAjenos >= 0		
		UtileriaDeParametros.validarParametro(peticion.getValorChequesAjenos(), "valorChequesAjenos", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
				
		//15 ● valorChequesExt >= 0		
		UtileriaDeParametros.validarParametro(peticion.getValorChequesExt(), "valorChequesExt", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	// VALIDACION No. 2
	/**
	 * M&eacutetodo para validar el arreglo de cheques obtenidos de la peticion  
	 * @param  cheques
	 * @throws ServicioException
	 */
	private void validacionCheques(ArrayList<Cheque> cheques ) throws ServicioException {
		logger.info( "[02. Validación de lista de cheques]");
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
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	// VALIDACION No. 3
	/**
	 * Método para validar existencia de transacción, producto, usuario en tablas de SFB. 
	 * @param datos
	 * @throws ServicioException
	 */
	private void validarSeguridadTerminalesFinancieros(DatosOperacion datos)  throws ServicioException{
		logger.info("[03. Invocar Función Soporte Seguridad]");
		
		//Invocación de la función de seguridad
		seguridadTerminalesFinancieros(datos);
		
		Date fechaSistema;
		try {
			fechaSistema = UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
		} catch (ParseException | TipoDatoException e ) {
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		} 
		
		//Recuperación de fecha de sistema en formato AAAAMMDD y su traslado al objeto "datos"
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistema);
		datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 4
	/**
	 * Método para validar el producto correspondiente al certificado y otras propiedades. 
	 * @param datos
	 * @throws ServicioException
	 */
	private void verificarProducto(DatosOperacion datos) throws ServicioException {
		logger.info("[04. Verificar producto]");		
		
		//Definición de parámetros 
	    Object[] paramsSELECT_SFBDB_AAMPR_PRODUCTOS_V4 = {
	    		datos.obtenerValor("codProducto")			
	    };
	    
		Map<String, Object> resultSetMap = null;
		Integer senProductoVista = null;
		Integer cantidadCuentas  = null;
		
		try {
			//Ejecución de sentencia SQL			
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAAMPR, parametros: " + Arrays.toString(paramsSELECT_SFBDB_AAMPR_PRODUCTOS_V4));
	    	resultSetMap =  jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAMPR_PRODUCTOS_V4), paramsSELECT_SFBDB_AAMPR_PRODUCTOS_V4);
			
			senProductoVista = ((BigDecimal)resultSetMap.get("senProductoVista")).intValue();
			cantidadCuentas =  ((BigDecimal)resultSetMap.get("cantidadCuentas")).intValue();
			
			//Traslado de propiedades al objeto "datos"
			datos.agregarDato("senProductoVista", senProductoVista);
			datos.agregarDato("cantidadCuentas", cantidadCuentas);
			
	    } catch (EmptyResultDataAccessException e) {
		   throw new ServicioException(20348, "Código de producto incorrecto");					
		}
		
	    if (UtileriaDeDatos.isEquals(senProductoVista ,Constantes.SI)) {
			   throw new ServicioException(20536, "Es una inversión a la vista");
	    }			
	}

	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
	// VALIDACION No. 5	
	// Se deben agregar los parametros del comprobante a datos
	/**
	 * Método para la recuperación de los datos del comprobante asociado a la apertura del certificado, emitido en plataforma.
	 * @param datos
	 * @throws ServicioException
	 */
		private void recuperarValidarDatosComprobante(DatosOperacion datos)   throws ServicioException{
		logger.info("[05. Recuperar datos del comprobante]");
		
		Comprobante dco = null;		
		FSRecuperarDatosComprobante fs = new FSRecuperarDatosComprobante(getJdbcTemplate(), getDbLinkValue());			

		//Se agregan propiedades requeridas al objeto datos para poder realizar la recuperación del comprobante
		datos.agregarDato("senReversa", Constantes.SI);
		datos.agregarDato("codTipoComprobante", Constantes.CD_COMPROBANTE_INGRESO);
		
		dco =  fs.recuperarDatosComprobante(datos);
		dco.setCuentaComprobante(dco.getCuentaComprobante().replaceAll("-", ""));

		logger.info("[04.1 Recuperar datos del comprobante]" + dco.getCodEstadoComprobante());
		//Se agrega objeto "dco" y sus propiedades al objeto "datos"
		datos.agregarDato("dco", dco);
		datos.agregarPropiedadesDeObjeto(dco);
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 6	
	/**
	 * Método para la recuperación de los datos de la cuenta del certificado asociado a los cupones a canelar.
	 * @param datos
	 * @throws ServicioException
	 */
    private void recuperarValidarDatosCuentaCertificado(DatosOperacion datos)  throws ServicioException{
		logger.info("[06. Recuperar datos del certificado en FFMDE]");   
		
		logger.info("cuentaComprobante",(String) datos.obtenerValor("cuentaComprobante"));

		String cuentaDeposito = ((String) datos.obtenerValor("cuentaComprobante"));
		String cuentaComprobante = ((String) datos.obtenerValor("cuentaComprobante")).replaceAll("-", "");
		datos.agregarDato("cuentaComprobante", cuentaComprobante);

		Certificado pcd = recuperarDatosCuentaCertificado((String) datos.obtenerValor("cuentaComprobante"));
		datos.agregarDato("pcd", pcd);
		datos.agregarPropiedadesDeObjeto(pcd);
		
		//Esta propiedad se agrega porque es necesaria para la respuesta del servicio
		datos.agregarDato("cuentaDeposito", cuentaDeposito);			
	}
    
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    
    // VALIDACION No. 7 	
    /**
 	 * Método para la recuperación de los datos del cliente titular del certificado.
 	 * @param datos
 	 * @throws ServicioException
 	 */
	private void recuperarValidarDatosCliente(DatosOperacion datos)  throws ServicioException{
		logger.info("[07. Recuperar Datos del Cliente]");		
		try {
		
			Cliente cliente = recuperarDatosCliente(datos.obtenerString("codCliente"));
			
			datos.agregarDato("cliente",cliente );
			datos.agregarPropiedadesDeObjeto(cliente);
			
			//Esta propiedad es necesaria para la respuesta, ya que los nombres se reconstruyen 
			//y el nombre del tipo de documento, depende del tipo de documento presentado por el cliente 
			datos.agregarDato("nombreCliente",cliente.getNombreModificadoCliente());
			datos.agregarDato("nomTipDocumentoCliente",cliente.getNombreDocumentoCliente());
			logger.info("[06. Recuperar Datos del Cliente]" + cliente.getNombreDocumentoCliente());	
			
		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}					
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    
    // VALIDACION No. 8 	
    /**
 	 * Método para la validación de oficinas donde se está ingresando el certificado vrs oficina donde se aperturó la cuenta.
 	 * @param datos
 	 * @throws ServicioException
 	 */	
	private void validarOficinas(DatosOperacion datos) throws ServicioException {
		logger.info("[08. Validar Oficinas]");		
		try {
			
			//Recuperación de objetos
			IngresoCertificadoReversaPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoReversaPeticion.class);			
			Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);

			//Definición de parámetros para la consulta y declaración de variables
			Object[] paramsSELECT_SFBDB_DAMOF_ZONA_ORIGEN_V8_1 = {pcd.getCodOficina()};
			Map<String, Object> resultSetMapOrigen = null;
			BigDecimal codZona = null;
			
			//Recuperando la zona de la oficina donde se aperturó la cuenta de certificado
		    try {
  				resultSetMapOrigen  =  jdbcTemplate.queryForMap(query(SELECT_SFBDB_DAMOF_ZONA_ORIGEN_V8_1), paramsSELECT_SFBDB_DAMOF_ZONA_ORIGEN_V8_1);
  				codZona = (BigDecimal) resultSetMapOrigen.get("codZona");  				

		    } catch (EmptyResultDataAccessException e) {
		    	throw new ServicioException(20110, "Agencia no existe");				
			}		
		    
			//Definición de parámetros para la consulta y declaración de variables
			Object[] paramsSELECT_SFBDB_DAMOF_ZONA_TRANS_V8_2 = {peticion.getCodOficinaTran()};
    	    Map<String, Object> resultSetMapTrans  =  null;
    	    BigDecimal codZonaTran = null;
    	   
			//Recuperando la zona de la oficina de la caja donde se está realizando el pago
		    try {
		    	resultSetMapTrans  =  jdbcTemplate.queryForMap(query(SELECT_SFBDB_DAMOF_ZONA_TRANS_V8_2), paramsSELECT_SFBDB_DAMOF_ZONA_TRANS_V8_2);
  				codZonaTran = (BigDecimal) resultSetMapTrans.get("codZonaTran");		    	
		    	
		    } catch (EmptyResultDataAccessException e) {
		    	throw new ServicioException(20110, "Agencia no existe");				
			}
		    
	     	//Validación del diseño
			 if (!UtileriaDeDatos.isEquals(codZonaTran,codZona)) {
				   throw new ServicioException(20145, "Acción restringida a la agencia de radicación");				 
			 }
			 
		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    
    // VALIDACION No. 9 	
    /**
 	 * Método para validar otros datos del certificado
 	 * @param datos
 	 * @throws ServicioException
 	 */		
	private void validarCertificadoDeposito(DatosOperacion datos)  throws ServicioException{
		logger.info("[09. Validar datos del Certificado de Depósito]");		
		try {
			//Recuperacion de objetos
			IngresoCertificadoReversaPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoReversaPeticion.class);
			Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);
			Comprobante dco = datos.obtenerObjeto("dco",Comprobante.class);
			
			logger.debug("Estado del certificado: " +  pcd.getCodEstadoCertificado());
			 if (!UtileriaDeDatos.isEquals(pcd.getCodEstadoCertificado(),Constantes.CD_ESTADO_DEPOSITADO)
					                                  &&
			     !UtileriaDeDatos.isEquals(pcd.getCodEstadoCertificado(),Constantes.CD_ESTADO_RETENIDO)) {
				   throw new ServicioException(20425, "Estado del depósito es incorrecto para operación");				 
			 }
			 
			 if (!UtileriaDeDatos.isEquals(pcd.getCantidadVecesImpreso(),new Integer(0)) ) {
				 throw new ServicioException(20016, "Estado incorrecto para revertir - CDP ya está impreso");				 
			 }

		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}			
	}

	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    
    // VALIDACION No. 10 	
    /**
 	 * Método para la actualización de señal de reversa de la transacción en AAATR y la inserción de perfiles AAATR
 	 * @param datos
 	 * @throws ServicioException
 	 */	
		private void actualizarTransaccionAAATR(DatosOperacion datos)  throws ServicioException{
			logger.info("[10. Actualizar transacción a reversar en AAATR]");		
			try {
				
				IngresoCertificadoReversaPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoReversaPeticion.class);
				Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);
				
			    Object[] paramsSELECT_SFBDB_AAATR_GLBDTIMEAAATR_V10_1 = {
			    		datos.obtenerValor("fechaRelativa"),
			    		peticion.getCodOficinaTran(),
			    		peticion.getCodTerminal(),
			    		peticion.getNumReversa(),
			    		peticion.getCodTran(),
			    		pcd.getCodProducto(),
			    		pcd.getCodOficina(),
			    		pcd.getNumCuenta(),
			    		pcd.getDigitoVerificador(),
			    		peticion.getNumDocumentoTran(),
			    		peticion.getValorMovimiento(),
			    		Constantes.SI
			    };
			    
				Map<String, Object> mapAAATR = null;
				Long glbDtimeAAATR = null;
				
				try {
				    logger.debug("[10.1 Ejecutando sentencia SELECT_SFBDB_AAATR_V10_1, parametros: " + Arrays.toString(paramsSELECT_SFBDB_AAATR_GLBDTIMEAAATR_V10_1));
					//Ejecución de sentencia SELECT_LINC_SFBDB_AAATR_V10 y recuperación de datos
					mapAAATR = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAATR_V10_1), paramsSELECT_SFBDB_AAATR_GLBDTIMEAAATR_V10_1);
										glbDtimeAAATR = ((BigDecimal)mapAAATR.get("glbDtimeAAATR")).longValue();

					//Se agrega dato recuperado al objeto "datos"
					datos.agregarDato("glbDtimeAAATR", glbDtimeAAATR);
					datos.agregarDato("glbDtime", glbDtimeAAATR);
					datos.agregarDato("codTerminalTran", peticion.getCodTerminal());
					datos.agregarDato("numTran", peticion.getNumReversa());
					
					
				} catch (EmptyResultDataAccessException erdae) {
					logger.error("No existe la operacion realizada...");
					throw new ServicioException(20212, "Transacción no aparece en Base de Datos");
				}
				
			    Object[] paramsUPDATE_SFBDB_AAATR_V10_2 = {
			    		Constantes.SI,		    		
			    		glbDtimeAAATR
			    };
			    
				//Ejecución de sentencia UPDATE_SFBDB_AAATR_V10_2 y recuperación de datos
			    logger.debug("[10.2 Ejecutando sentencia UPDATE_SFBDB_AAATR_V10_2, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_AAATR_V10_2));
				ejecutarSentencia(query(UPDATE_SFBDB_AAATR_V10_2), paramsUPDATE_SFBDB_AAATR_V10_2);
				
				//Se actualizan los perfiles de AAATR
				logger.debug("[10.3 Se invoca la función de actualización de perfiles de transacción");
				actualizarPerfilesTransaccionAAATR(datos);	
							    
			} catch (TipoDatoException e) {
				logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}				
		}
		
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	    
	    // VALIDACION No. 12	
	    /**
		 * M&eacutetodo para procesar l&oacutegica de pagos de cheques propios y retenciones
		 * @param  datos
		 * @throws ServicioException
		 * @throws TipoDatoException 
		 */
		private void ReversarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {
			logger.info("[12. Procesar cheques propios y retenciones]");

			IngresoCertificadoReversaPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoReversaPeticion.class);
			Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
			
			//Recuperando numTran del servicio en este paso para no perder valor,
			//porque se sustituye en FS Pagos cheques, y se setea al final
			Integer numReversa = datos.obtenerInteger("numReversa");
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
						datos.agregarDato("cuentaDestino", datos.obtenerValor("cuentaDeposito"));
						
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

			datos.agregarDato("numReversa", numReversa);
			datos.agregarDato("numDocumentoTran", numDocumentoTran);
			
		}
		
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	    
	    // VALIDACION No. 11 	
	    /**
	 	 * Método para la actualización de datos en COMPROBANTE (FFACO), CERTIFICADO (FFMDE) y PRODUCTOS (AAMPR)
	 	 * @param datos
	 	 * @throws ServicioException
	 	 */		
		private void actualizarDatosComprobanteCertificadoPorducto(DatosOperacion datos)  throws ServicioException {
			logger.info("[11. Actualizar Datos de Comprobante, Certificado y Producto]");
			try {
				
				IngresoCertificadoReversaPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoReversaPeticion.class);
				Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);
				Cliente cliente = datos.obtenerObjeto("cliente", Cliente.class);
		
				Object[] paramsUPDATE_SFBDB_FFACO_V11_1 = {
						Constantes.CD_COMPROBANTE_ESTADO_IMPRESO,
						peticion.getCodCajero(),
						datos.obtenerValor("horaSistema"),
						Constantes.CD_COMPROBANTE_INGRESO,
						peticion.getCodOficinaTran(),
						peticion.getCodProductoAux(),
						peticion.getNumDocumentoTran()
				};
				 
		        Integer registrosUpdateDatosComprobanteDeposito = (Integer)jdbcTemplate.update(query(UPDATE_SFBDB_FFACO_V11_1), paramsUPDATE_SFBDB_FFACO_V11_1);			
	        
				Object[] paramsUPDATE_SFBDB_FFMDE_V11_2 = {
						Constantes.CD_ESTADO_CAPTADO,
						datos.obtenerValor("fechaSistema"),
						pcd.getCodProducto(),
						pcd.getCodOficina(),
						pcd.getNumCuenta()
				};
				
		        Integer registrosUpdateDatosCertificadoDeposito = (Integer)jdbcTemplate.update(query(UPDATE_SFBDB_FFMDE_V11_2), paramsUPDATE_SFBDB_FFMDE_V11_2);			

		        
		        Integer cantidadCuentas = (Integer)datos.obtenerValor("cantidadCuentas") - 1;
			    Object[] paramsUPDATE_SFBDB_AAMPR_V11_3 = {
			    		cantidadCuentas,		    		
			    	    peticion.getCodProductoAux() 
			    };
			    
			    logger.debug("[11.6 Ejecutando sentencia UPDATE_LINC_SFBDB_AAATR_V12, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_AAMPR_V11_3));
				ejecutarSentencia(query(UPDATE_SFBDB_AAMPR_V11_3), paramsUPDATE_SFBDB_AAMPR_V11_3);
				
				datos.agregarDato("codCliente", cliente.getCodCliente());	//cgonzalez 23/01/2021	
	        
			} catch (TipoDatoException e) {
				logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}
		}
		

}