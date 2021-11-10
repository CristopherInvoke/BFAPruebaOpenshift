package sv.gob.bfa.ingresocertificado.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import sv.gob.bfa.ingresocertificado.model.IngresoCertificadoPeticion;
import sv.gob.bfa.ingresocertificado.model.IngresoCertificadoRespuesta;
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

/**
 * @author Claudia Gonzalez
 * @version 27/11/2019
 * La lógica para el ingreso de Certificado por Caja (FF200), se ha creado a partir del documento Disenio_Ingreso_Certificado_Caja_FF200
 * Permite realizar el ingreso de certificado por caja, a partir de un comprobante emitido en plataforma. 
 * Se crea un objeto principal llamado "datos", el cual se va llenando con todas las propiedades y objetos recuperados de cada actividad
 * o validación realizada, para ello se utilizan 2 funciones principales, de acuerdo a la necesidad: agregarDato, agregarPropiedades.
 * Se crea el objeto "peticion" El cual se crea a partir de la clase IngresoCertificadoPeticion, que son los campos que llenará la aplicación cliente.
 * Se crea el objeto "comprobante": El cual se crea partir de la función de soporte FSRecuperarDatosComprobante ubicada en svc-comunes 
 * Se crea el objeto "certificado": El cual se crea partir de la función de soporte FSRecuperarDatosCuentaCertificado ubicada en svc-comunes 
 * Se crea el objeto "respuesta":El cual se crea a partir de la clase IngresoCertificadoRespuesta, que son los campos que se enviarán a la aplicación cliente
 * Así mismo se declaran como constantes todas las sentencias SQL a ser consumidas tanto para CONSULTAS como para las ACTUALIZACIONES.
 * Se consumen métodos definidos en la clase Servicio (Ubicada en Comunes), como por ejemplo la ejecución de las consultas y actualizaciones 
 * */

/**
 * CLASE CORRESPONDIENTE IngresoCertificado
 */

public class IngresoCertificadoServicio  extends Servicio{

	//SENTENCIAS A CONSUMIR EN LA LÓGICA
	private final static String SELECT_SFBDB_AAMPR_PRODUCTOS_V4 = ""
			+ "	SELECT fse_vista AS senProductoVista, "
			+ "        anucuepro AS cantidadCuentas "
			+ "	  FROM linc.sfbdb_aampr@DBLINK@"
			+ "  WHERE aco_produ = ?"
			;	
	
	private final static String SELECT_NOMBRE_DOCUMENTO_TR_V8 = ""
			+ " SELECT ano_corta AS nomTipDocumentoPersona "
			+ "   FROM linc.sfbdb_bsmtg@DBLINK@  " 
			+ "  WHERE aco_tabla = 'DOC-IDENTI' "
			+ "    AND aco_codig = LPAD(?, 2, '0')"
			;
	
	private final static String SELECT_SFBDB_DAMOF_ZONA_ORIGEN_V10_1 = " "
			+ " SELECT dco_zona AS codZona" 
			+ "   FROM linc.sfbdb_damof@DBLINK@ " 
			+ "  WHERE dco_ofici =  ? " 
			;
	
	private final static String SELECT_SFBDB_DAMOF_ZONA_TRANS_V10_2 = " "
			+ " SELECT dco_zona AS codZonaTran, "
			+ "        dno_ofici AS nomOficina  "
			+ "   FROM linc.sfbdb_damof@DBLINK@ "
			+ "  WHERE dco_ofici = ?" 
			;	
	
	private final static String UPDATE_SFBDB_FFACO_V13_1 = ""
			+" UPDATE linc.sfbdb_ffaco@DBLINK@ "
			+"    SET fcoestcom = ?," 
			+"        fco_cajer = ?,"
			+"        fho_cajer = ? "
			+"  WHERE fco_tipco = ? " 
			+"    AND fcoofiter = ? " 
			+"    AND fcu_produ = ? "
			+"    AND fnu_compr = ? "
			;	
	
	private final static String UPDATE_SFBDB_FFMDE_V13_2= ""
			+" UPDATE linc.sfbdb_ffmde@DBLINK@ "
			+"    SET fco_estde = ?,"
			+"        ffeulttra = ? "
			+"  WHERE fcu_produ = ? "
			+"    AND fcu_ofici = ? "
			+"    AND fcunumcue = ? "
			;
	
	private final static String UPDATE_SFBDB_AAMPR_V13_3= ""
			+" UPDATE linc.sfbdb_aampr@DBLINK@ "
			+"    SET anucuepro = ?"
			+"  WHERE aco_produ = ? "
			;
	
	private final static String SELECT_FNC_CORREL_CANAL_V14 = ""
			+" SELECT MADMIN.FNC_CORREL_CANAL(?) AS numTran "
			+"   FROM DUAL"
			;	

	private final static String UPDATE_SFBDB_AAARP_V15 = "" 
			+ "UPDATE LINC.SFBDB_AAARP@DBLINK@" 
			+ "   SET ACO_CAUSA = ?,"
			+ "       ACO_CONCE = ?," 
			+ "       ACU_OFICI = ?," 
			+ "       ACU_PRODU = ?," 
			+ "       ACUNUMCUE = ?,"
			+ "       ACUDIGVER = ?," 
			+ "       DCOTERADI = ?," 
			+ "       DCOTERUSO = ?," 
			+ "       SCO_ESTAD = ?,"
			+ "       SCOOFIUSO = ?," 
			+ "       SCOUSUUSO = ?," 
			+ "       SFE_USO   = ?," 
			+ "       SHO_USO   = ?,"
			+ "       TNUDOCTR2 = ?," 
			+ "       TNUDOCTRA = ?," 
			+ "       TVA_EFECT = ?," 
			+ "       TVA_MOVIM = ?,"
			+ "       TVA_VALOR = ?," 
			+ "       TNU_TRANS = ? " 
			+ " WHERE scoregpre = ? "
			;	
		
	
	Logger logger = LoggerFactory.getLogger(IngresoCertificadoServicio.class);

	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * METODO PRINCIPAL, CONTIENE TODA LA LOGICA DE NEGOCIO
	 */
	@Transactional("transactionManager")
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		logger.info("[Iniciando servicio Ingreso de Certificado - FF200]");	
		
		logger.info("[Creando objeto Datos Operacion]");
		DatosOperacion datos = crearDatosOperacion();	
		
		logger.info("[Cast de objeto de dominio -> IngresoCertificadoPeticion]");			
		IngresoCertificadoPeticion peticion = (IngresoCertificadoPeticion) objetoDom;
		
		//Una vez creada la petición, se agrega dicho objeto al objeto principal "datos"
		datos.agregarDato("peticion", peticion);
		datos.agregarPropiedadesDeObjeto(peticion);
		datos.agregarDato("codProducto",peticion.getCodProductoAux());
		
		//Se realiza el llamado de cada metodo que se traduce practicamente a cada actividad descrita en el documento de diseño
		try {		
			
			// 1. Validar Parámetros
			// VALIDACION No. 1
			validacionInicial(peticion);		
			
			// VALIDACION No. 2
			// 2. Validar Cheques
			validacionCheques((ArrayList<Cheque>) peticion.getCheques(), datos);
			
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
			//8. Recuperar Documento Persona que Realiza Transacción.		
			validarDocumentoPersonaTR(datos);
			
			// VALIDACION No. 9	
			//9. Definir Variables Auxiliares
			definirVariablesAuxiliares(datos);
			
			// VALIDACION No. 10		
			//10. Validar Oficinas
			validarOficinas(datos);
			
			// VALIDACION No. 11
			//11. Validar datos del Certificado de Depósito
			validarDatosCertificadoDeposito(datos);
			
			// VALIDACION No. 12
			//12. Validar parámetros de Lavado de Dinero
			validarParametrosLavadoDinero(datos);
			
			// VALIDACION No. 13
			//13. Actualizar Datos de Comprobante, Certificado y Producto
			actualizarDatosComprobanteCertificadoProducto(datos);
	
			// VALIDACION No. 14		
			//14. Recuperar Número de Transacción		
			validarObtenerNumeroTransaccion(datos);		
	
			// VALIDACION No. 15		
			//15. Actualizar Datos UIF
			actualizandoRegistroUIF(datos);		
			
			// VALIDACION No. 16
			// 16. Registrar Transacción en tabla AAATR
			registroTransaccionAAATR(datos);
			
			// VALIDACION No. 17
			// 17. Registrar Transacción en tabla AAATR
			procesarChequesPropiosRetencionesGerencia(datos);

			logger.debug("Preparando objeto de respuesta ...");
			
			IngresoCertificadoRespuesta respuesta = new IngresoCertificadoRespuesta(); 
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("Exito");
			
			if(logger.isDebugEnabled()) {
				logger.debug("RESPUESTA IngresoCertificado : {} ", respuesta);
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
	private void validacionInicial(IngresoCertificadoPeticion peticion)  throws ServicioException{
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

		//6. ● tipDocumentoPersona > 0
		UtileriaDeParametros.validarParametro(peticion.getTipDocumentoPersona(), "tipDocumentoPersona", TipoValidacion.ENTERO_MAYOR_CERO);			

		//7. ● numDocumentoPersona diferente vacío o espacios		
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoPersona(), "numDocumentoPersona", TipoValidacion.CADENA_VACIA);

		//8. ● nombrePersona diferente vacío o espacios		
		UtileriaDeParametros.validarParametro(peticion.getNombrePersona(), "nombrePersona", TipoValidacion.CADENA_VACIA);			
		
		//9. ● Si numTransLavado es diferente de vacío entonces validar numTransLavado >= 0		
		UtileriaDeParametros.validarParametro(peticion.getNumTransLavado(), "numTransLavado", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);			
		
		//10. ● codTran > 0
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);

		//11. ● senSupervisor	in (1,2)
		if (!UtileriaDeDatos.estaIncluidoEn(peticion.getSenSupervisor(), new Integer[] { 1, 2 })) {
			throw new ServicioException(21010, "Señal del supervisor debe ser 1 o 2", "señal Supervisor");
		}			

		//12.  ● numCaja > 0		
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);			

		//13. ● codCajero diferente vacío o espacios
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);			

		//14. ● codTerminal > 0
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);			
		
		//15. ●  codOficinaTran > 0
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//16. ● valorChequesPropios >= 0
		UtileriaDeParametros.validarParametro(peticion.getValorChequesPropios(), "valorChequesPropios", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
		//17. ● valorChequesAjenos >= 0		
		UtileriaDeParametros.validarParametro(peticion.getValorChequesAjenos(), "valorChequesAjenos", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
		//18. ● valorChequesExt >= 0		
		UtileriaDeParametros.validarParametro(peticion.getValorChequesExt(), "valorChequesExt", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
	}
	
	// VALIDACION No. 2	
	/**
	 * M&eacutetodo para validar el arreglo de cheques obtenidos de la peticion  
	 * @param  cheques
	 * @param datos 
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void validacionCheques(ArrayList<Cheque> cheques, DatosOperacion datos ) throws ServicioException, TipoDatoException {
		
		IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion",IngresoCertificadoPeticion.class);
		
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
	//Se deben agregar los parametros del comprobante a datos
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
		datos.agregarDato("senReversa", Constantes.NO);
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
	 * Método para la recuperación de los datos de la cuenta del certificado a ingresar.
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
			
			datos.agregarDato("cliente",cliente);
			datos.agregarPropiedadesDeObjeto(cliente);
			
			//Esta propiedad es necesaria para la respuesta, ya que los nombres se reconstruyen 
			//y el nombre del tipo de documento, depende del tipo de documento presentado por el cliente 
			datos.agregarDato("nombreCliente",cliente.getNombreModificadoCliente());
			datos.agregarDato("nomTipDocumentoCliente",cliente.getNombreDocumentoCliente());
			logger.info("[06. Recuperar Datos del Cliente]" + cliente.getNombreDocumentoCliente());	
			
		} catch (TipoDatoException e) {
			logger.error("Error inesperado: " + e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		} 				
	} 
    
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 8
    /**
 	 * Método para obtener el documento de la persona que realiza la transacción.
 	 * @param datos
 	 * @throws ServicioException
 	 */
	private void validarDocumentoPersonaTR(DatosOperacion datos) throws ServicioException {
		logger.info("[08. Recuperar Documento Persona que Realiza Transacción]");		
		 
		try {
            //Recuperación de objeto
			IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);			
			
			//Definición de parámetro para ejecutar entencia SQL
			Object[] paramsDocumentoPersonaTR_V8 = { peticion.getTipDocumentoPersona() };
			
			//Ejecución de consulta
			String nomTipDocumentoPersona = jdbcTemplate.queryForObject(query(SELECT_NOMBRE_DOCUMENTO_TR_V8), String.class,
					paramsDocumentoPersonaTR_V8);
		
			//Variable a ser insertada en AAATR
			String descripcionTran = "COD.ID: " + peticion.getTipDocumentoPersona() + " " + nomTipDocumentoPersona + " NOMBRE TERCERO: "
					+ peticion.getNombrePersona();
			
			datos.agregarDato("nomTipDocumentoPersona", nomTipDocumentoPersona);
			datos.agregarDato("descripcionTran", descripcionTran);

		} catch (TipoDatoException e) {
			logger.error("Error inesperado: " + e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		} 
	}	

	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 9
 	/**
	 * Método para definir propiedades necesarias para el servicio
	 * @param datos
	 * @throws ServicioException
	 */
	private void definirVariablesAuxiliares(DatosOperacion datos) throws ServicioException{
		logger.info("[09. Definir Variables Auxiliares]");
		
		datos.agregarDato("codCausal", datos.obtenerValor("codTran"));			
		datos.agregarDato("codConcepto", Constantes.CONCEPTO_CD);
		datos.agregarDato("codDebCre", Constantes.CREDITO);
	}
	
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 10	
 	/**
	 * Método para validar la oficina donde se realiza la transacción vrs oficina a la que pertenece la cuenta de certificado
	 * @param datos
	 * @throws ServicioException
	 */
	private void validarOficinas(DatosOperacion datos) throws ServicioException {
		logger.info("[10. Validar Oficinas]");		
		try {
			
			//Recuperación de objetos
			IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);			
			Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);
			
			//Definición de parámetros para la consulta y declaración de variables
			Object[] paramsSELECT_SFBDB_DAMOF_ZONA_ORIGEN_V10_1 = {pcd.getCodOficina()};
			Map<String, Object> resultSetMapOrigen = null;
			BigDecimal codZona = null;
			
			//Recuperando la zona de la oficina donde se aperturó la cuenta de certificado
		    try {
  				resultSetMapOrigen  =  jdbcTemplate.queryForMap(query(SELECT_SFBDB_DAMOF_ZONA_ORIGEN_V10_1), paramsSELECT_SFBDB_DAMOF_ZONA_ORIGEN_V10_1);
  				codZona = (BigDecimal) resultSetMapOrigen.get("codZona");  				

		    } catch (EmptyResultDataAccessException e) {
		    	throw new ServicioException(20110, "Agencia no existe");				
			}
		    
			//Definición de parámetros para la consulta y declaración de variables
			Object[] paramsSELECT_SFBDB_DAMOF_ZONA_TRANS_V10_2 = {peticion.getCodOficinaTran()};
			Map<String, Object> resultSetMapTrans  =  null;
    	    BigDecimal codZonaTran = null;
    	    
			//Recuperando la zona de la oficina de la caja donde se está realizando el pago
		    try {
		    	resultSetMapTrans  =  jdbcTemplate.queryForMap(query(SELECT_SFBDB_DAMOF_ZONA_TRANS_V10_2), paramsSELECT_SFBDB_DAMOF_ZONA_TRANS_V10_2);
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
	
	// VALIDACION No. 11	
 	/**
	 * Método para validar datos de la cuenta del certificado.
	 * @param datos
	 * @throws ServicioException
	 */
	private void validarDatosCertificadoDeposito(DatosOperacion datos)  throws ServicioException{
		logger.info("[11. Validar datos del Certificado de Depósito]");		
		try {
			
			//Recuperación de objetos
			IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);
			Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);
			Comprobante dco = datos.obtenerObjeto("dco",Comprobante.class);
			
			//Validaciones del diseño
			 if (!UtileriaDeDatos.isEquals(pcd.getCodEstadoCertificado(),Constantes.CD_ESTADO_CAPTADO)) {
				   throw new ServicioException(20425, "Estado del depósito es incorrecto para operación");				 
			 }
			 
			 if (!UtileriaDeDatos.isEquals(peticion.getValorEfectivo(), dco.getValorEfectivoRecibido())
					                                  ||
   				 !UtileriaDeDatos.isEquals(peticion.getValorCheques(), dco.getValorChequesRecibido())					 
                                                      ||					 
   				 !UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), dco.getValorMovimientoComprobante())){
					 throw new ServicioException(20419, "Valor del depósito no corresponde");				 
			}
			 
			 if (UtileriaDeDatos.isEquals(pcd.getSenAperturaMultiple(),Constantes.SI) ) {
				 throw new ServicioException(20617, "La apertura no corresponde");				 
			 }

		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}			
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
	// VALIDACION No. 12
 	/**
	 * Método para validar parámetros de lavado de dinero
	 * @param datos
	 * @throws ServicioException
	 */
	private void validarParametrosLavadoDinero(DatosOperacion datos)  throws ServicioException {
		logger.info("[12. Validar parámetros de Lavado de Dinero]");
		try {
			
			//Recuperación de objeto
			IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);
		
			datos.agregarDato("numTransLavado", peticion.getNumTransLavado());
			datos.agregarDato("tipDocumentoPersona",peticion.getTipDocumentoPersona() );
			datos.agregarDato("numDocumentoPersona", peticion.getNumDocumentoPersona());
			datos.agregarDato("nombrePersona", peticion.getNombrePersona());			
			datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
			datos.agregarDato("valorCheques", peticion.getValorCheques());			
						
			//Invocación de función para validar parámetros de lavado de dinero
			validarRegistroUIF(datos);
		
		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}		
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
	// VALIDACION No. 13
	/**
	 * Método para la actualización de Comprobante, Certificado y Producto
	 * @param datos
	 * @throws ServicioException
	 */
	private void actualizarDatosComprobanteCertificadoProducto(DatosOperacion datos)  throws ServicioException {
		logger.info("[13. Actualizar Datos de Comprobante, Certificado y Producto]");
		try {
			
			//Recuperación de objetos
			IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);
			Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);
			Integer estadoCDP = Constantes.CD_ESTADO_CAPTADO;
			
			logger.debug("Monto de valores ajenos" + peticion.getValorChequesAjenos());
	
			 if (UtileriaDeDatos.isGreater(peticion.getValorChequesAjenos(),BigDecimal.ZERO)) {
				 estadoCDP = Constantes.CD_ESTADO_RETENIDO;
			 }else {
				 estadoCDP = Constantes.CD_ESTADO_DEPOSITADO;
			 }
			 
			 datos.agregarDato("estadoCDP", estadoCDP);
			 
			 logger.debug("Estado del cdp: " + pcd.getCodEstadoCertificado());
			
			//Definición de parámetros para ejecución de sentencia SQL
			Object[] paramsUPDATE_SFBDB_FFACO_DATOS_V13_1 = {
					Constantes.CD_COMPROBANTE_ESTADO_ACTIVO,
					peticion.getCodCajero(),
					datos.obtenerValor("horaSistema"),
					Constantes.CD_COMPROBANTE_INGRESO,
					peticion.getCodOficinaTran(),
					peticion.getCodProductoAux(),
					peticion.getNumDocumentoTran()
			};
			Integer registrosUpdateDatosComprobanteDeposito = (Integer)jdbcTemplate.update(query(UPDATE_SFBDB_FFACO_V13_1), paramsUPDATE_SFBDB_FFACO_DATOS_V13_1);			
        
			//Definición de parámetros para ejecución de sentencia SQL
			Object[] paramsUPDATE_SFBDB_FFMDE_V13_2 = {
					estadoCDP,
					datos.obtenerValor("fechaSistema"),
					pcd.getCodProducto(),
					pcd.getCodOficina(),
					pcd.getNumCuenta()
			};			
	        Integer registrosUpdateDatosCertificadoDeposito = (Integer)jdbcTemplate.update(query(UPDATE_SFBDB_FFMDE_V13_2), paramsUPDATE_SFBDB_FFMDE_V13_2);
	        
			//Definición de parámetros para ejecución de sentencia SQL
	        Integer cantidadCuentas = (Integer)datos.obtenerValor("cantidadCuentas") + 1;
		    Object[] paramsUPDATE_SFBDB_AAMPR_V13_3 = {
		    		cantidadCuentas,		    		
		    	    peticion.getCodProductoAux() 
		    };
		    
		    logger.debug("[13.1 Ejecutando sentencia UPDATE_LINC_SFBDB_AAATR_V12, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_AAMPR_V13_3));
			ejecutarSentencia(query(UPDATE_SFBDB_AAMPR_V13_3), paramsUPDATE_SFBDB_AAMPR_V13_3);
	        
		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}		
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
	// VALIDACION No. 14
	/**
	 * Método la recuperación del número de transacción, que corresponde al TNU_TRANS de AAATR
	 * @param datos
	 * @throws ServicioException
	 */
	private void validarObtenerNumeroTransaccion(DatosOperacion datos) throws ServicioException {
		logger.info("[14. Recuperar Número de Transacción]");
		
		Integer numTran = (Integer) jdbcTemplate.queryForObject(query(SELECT_FNC_CORREL_CANAL_V14), Integer.class,Constantes.VENTANILLA);
		datos.agregarDato("numTran", numTran);
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
	// VALIDACION No. 15	
	/**
	 * Método para la actualización de registro en AAARP, correspondiente a UIF (Lavado de dinero)
	 * @param datos
	 * @throws ServicioException
	 */
	private void actualizandoRegistroUIF(DatosOperacion datos) throws ServicioException {
		logger.info("[15. Actualizar Datos UIF]");
		try {
			
			//Recuperación de objetos
			IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);
			Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);

			if (UtileriaDeDatos.isGreater(peticion.getNumTransLavado(), new Integer(0))) {
				
				//Definición de parámetros para ejecución de sentencia SQL
				Object[] paramsSFBDBAAARP_V15 = 
					{ 
						datos.obtenerValor("codCausal"),
						datos.obtenerValor("codConcepto"),						
						pcd.getCodOficina(), 
						pcd.getCodProducto(), 
						pcd.getNumCuenta(), 
						pcd.getDigitoVerificador(),
						peticion.getCodTerminal(),						
						peticion.getCodTerminal(),
						Constantes.UIF_USADO,
						peticion.getCodOficinaTran(), 
						peticion.getCodCajero(), 
						datos.obtenerValor("fechaSistemaAMD"),
						datos.obtenerValor("horaSistema"),						
						new Integer(0),
						peticion.getNumDocumentoTran(),
						peticion.getValorEfectivo(), 
						peticion.getValorEfectivo(),
						new Integer(0),
						datos.obtenerValor("numTran"),
						peticion.getNumTransLavado() };		
				
						Integer registrosUpdateParamsSFBDBAAARP = (Integer)jdbcTemplate.update(query(UPDATE_SFBDB_AAARP_V15),paramsSFBDBAAARP_V15);
				}

		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 16	
	/**
	 * Método para el registro de la transacción en AAAATR
	 * @param datos
	 * @throws ServicioException
	 */
	private void registroTransaccionAAATR(DatosOperacion datos) throws ServicioException {
		logger.info("[16. Registrar Transacción en tabla AAATR]");
		
		try {
			
			//Recuperación de objetos
			IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);
			Certificado pcd = datos.obtenerObjeto("pcd", Certificado.class);			
			Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);			

			datos.agregarPropiedadesDeObjeto(peticion);
			datos.agregarPropiedadesDeObjeto(cliente);
			
			datos.agregarDato("codCausal", datos.obtenerValor("codCausal"));			
			datos.agregarDato("codConcepto", datos.obtenerValor("codConcepto"));
			datos.agregarDato("codOficina", pcd.getCodOficina());			
			datos.agregarDato("codProducto", pcd.getCodProducto());			
			datos.agregarDato("numCuenta", pcd.getNumCuenta());			
			datos.agregarDato("digitoVerificador", pcd.getDigitoVerificador());			
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());			
			datos.agregarDato("codTerminal", peticion.getCodTerminal());			
			datos.agregarDato("fechaRelativa", datos.obtenerValor("fechaRelativa"));			
			datos.agregarDato("horaTran", datos.obtenerValor("horaSistema"));						
			datos.agregarDato("numTran", datos.obtenerValor("numTran"));			
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());						
			datos.agregarDato("codCompania", datos.obtenerValor("codCompania"));
			datos.agregarDato("codMoneda",pcd.getCodMoneda());
			datos.agregarDato("numCaja",peticion.getNumCaja());
			datos.agregarDato("montoIVA", new BigDecimal(0)); 
			datos.agregarDato("codTran", peticion.getCodTran());			
			datos.agregarDato("codCajero", peticion.getCodCajero());			
			datos.agregarDato("codDebCre", datos.obtenerValor("codDebCre"));									
			datos.agregarDato("numSecuenciaCupon", new Integer(0));
			datos.agregarDato("valorImpuestoVenta", new BigDecimal(0));						
			datos.agregarDato("codSectorEconomico", cliente.getCodSectorEconomicoCliente());
			datos.agregarDato("numDiasAtras", new Integer(0));						
			datos.agregarDato("fechaSistema", datos.obtenerValor("fechaSistema"));
			datos.agregarDato("fechaTran", datos.obtenerValor("fechaSistema"));			
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
			datos.agregarDato("valorAnterior", new BigDecimal(0));									
			datos.agregarDato("valorCompra", new BigDecimal(0));
			datos.agregarDato("valorMovimiento",peticion.getValorMovimiento());
			datos.agregarDato("valorCheque",peticion.getValorCheques());						
			datos.agregarDato("valorVenta", new BigDecimal(0));
			datos.agregarDato("numDocumentoTran2", null);						
			datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());			
			datos.agregarDato("valorChequesExt", peticion.getValorChequesExt());
			datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());			
			datos.agregarDato("descripcionTran", datos.obtenerValor("descripcionTran"));						
			datos.agregarDato("codBancoTransf", " ");			
			datos.agregarDato("codPaisTransf", " ");			
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("codCliente", pcd.getCodCliente());						
			datos.agregarDato("valorImpuesto", new BigDecimal(0));						
			datos.agregarDato("tipDocumentoCliente", cliente.getTipDocumentoCliente());
			datos.agregarDato("numDocumentoCliente", cliente.getNumDocumentoCliente());			
			datos.agregarDato("numDocumentoImp", new Integer(0));						
			datos.agregarDato("codSubCausal", new Integer(0));						
			
			registrarTransaccionAAATR(datos);

		} catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
	}
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	// VALIDACION No. 17		
	/**
	 * M&eacutetodo para procesar logica de pagos de cheques propios y retenciones
	 * @param  datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void procesarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {
		logger.info("[17. Procesar cheques propios, retenciones y Gerencia]");


		IngresoCertificadoPeticion peticion = datos.obtenerObjeto("peticion", IngresoCertificadoPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
	
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
					
					logger.info("[17.1 Procesar cheques propios]");
					datos.agregarDato("codTran", chk.getCodTran());
					datos.agregarDato("codCausal", chk.getCodCausal());
					datos.agregarDato("codPantalla", chk.getCodPantalla());
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					datos.agregarDato("autMaxDiasCheque", chk.getAutMaxDiasCheque());
					datos.agregarDato("numAutorizacion", chk.getNumAutorizacion());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));
					datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
					
					datos.agregarDato("tipDocumentoPersona", peticion.getTipDocumentoPersona());
					datos.agregarDato("numDocumentoPersona", peticion.getNumDocumentoPersona());
					datos.agregarDato("nombrePersona", peticion.getNombrePersona());

					pagosCheques(datos);
					chk.setNumTran(datos.obtenerInteger("numTran"));
					chk.setCodCajero(datos.obtenerString("codCajero"));
					chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
					chk.setNomOficina(datos.obtenerString("nomOficina"));
				} 
				else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 3) ||
							UtileriaDeDatos.isEquals(chk.getTipCheque(), 4)){
					
					logger.info("[17.2 Procesar cheques Ajenos o Exterior]");
					logger.debug("CUENTA TRANSACCION - CUENTA DESTINO ", datos.obtenerValor("cuentaDeposito"));
					datos.agregarDato("cuentaDestino", datos.obtenerValor("cuentaDeposito"));
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
		datos.agregarDato("codPantalla", codPantalla);
		
		datos.agregarDato("numTran", numTran);
		datos.agregarDato("numDocumentoTran", numDocumentoTran);
		datos.agregarDato("codCliente", cliente.getCodCliente());	//cgonzalez 23/01/2021	
	}
}   