package sv.gob.bfa.pagocomision.servicio;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import sv.gob.bfa.core.model.Certificado;
import sv.gob.bfa.core.model.Cheque;
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
import sv.gob.bfa.pagocomision.model.PagoComisionPeticion;
import sv.gob.bfa.pagocomision.model.PagoComisionRespuesta;


/**
 * @author Claudia Gonzalez
 * @version 10/1/2020
 * La lógica para Pago de Comision (AJ209), se ha creado a partir del documento Disenio_Pago_Comision_AJ209
 * Permite realizar pago de comisión, como un cargo del Banco hacia un cliente.
 * Se crea un objeto principal llamado "datos", el cual se va llenando con todas las propiedades y objetos recuperados de cada actividad
 * o validación realizada, para ello se utilizan 2 funciones principales, de acuerdo a la necesidad: agregarDato, agregarPropiedades.
 * Se crea el objeto "peticion" El cual se crea a partir de la clase PagoComisionPeticion, que son los campos que llenará la aplicación cliente.
 * Se crea el objeto "respuesta":El cual se crea a partir de la clase PagoComisionRespuesta, que son los campos que se enviarán a la aplicación cliente
 * Así mismo se declaran como constantes todas las sentencias SQL a ser consumidas tanto para CONSULTAS como para las ACTUALIZACIONES.
 * Se consumen métodos definidos en la clase Servicio (Ubicada en Comunes), como por ejemplo la ejecución de las consultas y actualizaciones 
 * 
 * 10/02/2020
 * Se agregan mejoras para validar cuenta ingresada y que sea la misma que ingresó en el módulo de IVA
 * */
public class PagoComisionServicio extends Servicio{
	
	private static final String SELECT_SFBDB_IVMVE_V6 = ""
			+" SELECT NVL(bfe_pago,  0) AS fechaPago,"
			+"        NVL(bvaafecta, 0) AS valorGravado,"
			+"        NVL(bvaexento, 0) AS valorExento,"
			+"        NVL(bco_causa, 0) AS codCausal,"
			+"        NVL(bco_trans, 0) AS codTranIVMVE,"
			+"        NVL(bva_iva,   0) AS valorIva,"
			+"        NVL(bnu_trans, 0) AS numTran,"
			+"        NVL(bcu_produ, 0) AS codProducto,"
			+"        NVL(bcu_ofici, 0) AS codOficina,"			
			+"        NVL(bcunumcue, 0) AS numCuenta,"
			+"        NVL(bcudigver, 0) AS digitoVerificador,"
			+"        NVL(bnufacfis, 0) AS numeroCorrelativo,"
			+"        NVL(bnudoctra, 0) AS numeroDocumento"   
			+"  FROM linc.sfbdb_ivmve@DBLINK@"
			+" WHERE bcodonant = ?"
			+"   AND bcofacfis = ?"
			+"   AND bnufacfis = ?"
			;
	private final static String SELECT_SFBDB_AAATR_V7 =""
			+ "SELECT count(glb_dtime) as cantidadRegistros"
			+ "  FROM linc.sfbdb_aaatr@DBLINK@"
			+ " WHERE tfetrarel  = ?"
			+ "   AND dco_ofici  = ?"
			+ "   AND dco_termi  = ?"
			+ "   AND tnu_trans  = ?"
			+ "   AND dco_trans  = ?"
			+ "   AND aco_causa  = ?"
			+ "   AND tnudoctra  = ?"
			+ "   AND tva_movim  = ?"
			+ "   AND tse_rever  <> ?"
			;
	
	private final static String SELECT_FNC_CORREL_CANAL_V8 = ""
			+" SELECT MADMIN.FNC_CORREL_CANAL(?) AS numTran "
			+"   FROM DUAL"
			;
	
	private static final String UPDATE_SFBDB_IVMVE_V9_1= 
			"UPDATE LINC.SFBDB_IVMVE@DBLINK@" + 
			"   SET BNU_CAJA  = ?," + 
			" 		BCO_TEPAG = ?," + 
			" 		BCO_OFPAG = ?," + 
			"       BFE_PAGO  = ?," +
			" 		BNUDOCTRA = ?," + 
			" 		BCO_TRANS = ?," + 
			" 		BNU_TRANS = ?" + 
			" WHERE BCODONANT = ?" + 
			"   AND BCOFACFIS = ?" + 
			"   AND BNUFACFIS = ?"
			;
		
	
	private final static String SELECT_SFBDB_AAMPR_V9_2 = ""
			+ " SELECT NVL(aco_conce, 0) AS codConceptoAAMPR"
			+ "   FROM linc.sfbdb_aampr@DBLINK@  " 
			+ "  WHERE aco_produ = ?"
			;	
	
	private static final String UPDATE_SFBDB_PPRGP_V9_3= 
			"UPDATE LINC.SFBDB_PPRGP@DBLINK@" + 
			"   SET pco_estad = ? " + 
			" WHERE pcu_ofici = ? " + 
			" 	AND	pcu_produ = ? " + 
			" 	AND	pcunumcue = ? " + 
			" 	AND psecobant = ? " + 
		//	" 	AND	pcunumcue = ?" + 
			" 	AND	pnucompro = ? " 
			;
	private static final String SELECT_SFBDB_PPRGP_V9_4= 
            " SELECT COUNT(*) as cantidadRegistros  " + 
			" FROM LINC.SFBDB_PPRGP@DBLINK@ " + 
			" WHERE pcu_ofici = ? " + 
			" 	AND	pcu_produ = ? " + 
			" 	AND	pcunumcue = ? " + 
			" 	AND psecobant = ? " + 
		//	" 	AND	pcunumcue = ? " + 
			" 	AND	pnucompro = ? " 
			;
	
	private static final String SELECT_SFBDB_PPRGP_V9_5= 
            " SELECT COUNT(*) as cantidadRegistros  " + 
			" FROM LINC.SFBDB_PPRGP@DBLINK@ " + 
			" WHERE pcu_ofici = ? " + 
			" 	AND	pcu_produ = ? " + 
			" 	AND	pcunumcue = ? " + 
			" 	AND psecobant = ? " + 
			" 	AND	pnucompro = ? " +
			"   AND pco_estad = 3 "
			;
	
	private static final String SELECT_SFBDB_PPASO="SELECT count(*) as cantidadRegistros FROM LINC.SFBDB_PPASO@DBLINK@ WHERE PCU_PRODU=?  AND PCU_OFICI=?  AND  PCUNUMCUE=?";
	
	private static final String SELECT_IVMDE="SELECT SUM(BVAAFECTA) valorGravadoTotal,SUM(BVAEXENTO) valorExentoTotal from linc.sfbdb_IVMDE@DBLINK@ where bcofacfis=? and bnufacfis=?";
	

	Logger logger = LoggerFactory.getLogger(PagoComisionServicio.class);
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * METODO PRINCIPAL, CONTIENE TODA LA LOGICA DE NEGOCIO
	 */
	@Transactional("transactionManager")
	@Override
	public Object procesar(Object objetoDom) throws ServicioException{
		logger.info("Iniciando servicio PagoComision AJ209");
		
		logger.debug("Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug("Cast de objeto de dominio -> PagoComisionPeticion ");
		PagoComisionPeticion peticion = (PagoComisionPeticion) objetoDom;
		
		//Una vez creada la petición, se agrega dicho objeto al objeto principal "datos"
		datos.agregarDato("peticion", peticion);
		datos.agregarPropiedadesDeObjeto(peticion);
		
		//Se realiza el llamado de cada metodo que se traduce practicamente a cada actividad descrita en el documento de diseño
		try {
			
			// VALIDACION No. 1
			// 1. Validar Parámetros
			validacionInicial(peticion);
			
			// VALIDACION No. 2
			// 2. Validar Cheques
			validacionCheques((ArrayList<Cheque>) peticion.getCheques(), datos);
			
			// VALIDACION No. 3
			// 3. Invocar Función Soporte Seguridad		
			validarSeguridadTerminalesFinancieros(datos);
			
			// VALIDACION No. 4
			//4. Definir Variables Auxiliares 
			definirVariablesAuxiliares(datos);
			
			// VALIDACION No. 5
			// 5. Registrar Transacción en tabla AAATR
			validarExistenciaComprobantePago(datos);
			
			// VALIDACION No. 6
			// 6. Validar Existencia de Solicitud de Crédito o Recuperar Datos de la Cuenta
			if (UtileriaDeDatos.isEquals(datos.obtenerInteger("codCausal"),200)) {
				validarExistenciaSolicitoCredito(datos);
			} else{ 
				recuperarDatosCuenta(datos);
			}
			
			// VALIDACION No. 7
			// 7. Validar existencia de transacción en AAATR
			validarExistenciaTransaccionAAATR(datos);
			
		   // VALIDACION No. 8
			// 8. Registrar Transacción en tabla AAATR
			registrarDatosAAATR(datos);

			// VALIDACION No. 9
			// 9. Registrar Transacción en tabla AAATR
			actualizarDatosMaestroFactura(datos);
			
			// VALIDACION No. 10
			// 10. Registrar Transacción en tabla AAATR
			procesarChequesPropiosRetencionesGerencia(datos);

			
			logger.debug("Preparando objeto de respuesta ...");
			
			PagoComisionRespuesta respuesta = new PagoComisionRespuesta(); 
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			if(logger.isDebugEnabled()) {
				logger.debug("RESPUESTA PagoComision : {} ", respuesta);
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
	private void validacionInicial(PagoComisionPeticion peticion) throws ServicioException{
		logger.info("[01. Validar Parámetros]");
		logger.debug("Peticion recibida: {}", peticion);
		
		//1. ● numDocumentoTran > 0
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//2. ● senCreditoFiscal (1,2)
		UtileriaDeParametros.validarParametro(peticion.getSenCreditoFiscal(), "senCreditoFiscal", TipoValidacion.ENTERO_VALOR_EN,  new Integer[] {1,2});
		
		//3. ● valorEfectivo >= 0
		UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);

		//4. ● valorCheque >= 0
		UtileriaDeParametros.validarParametro(peticion.getValorCheques(), "valorCheque", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
		//5. ● valorMovimiento > 0
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		
		//6. ● codTran > 0		
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//7. ● senSupervisor (1,2)
		UtileriaDeParametros.validarParametro(peticion.getSenSupervisor(), "senSupervisor", TipoValidacion.ENTERO_VALOR_EN,  new Integer[] {1,2});
		
		//8. ● codOficinaTran > 0
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//9. ● codTerminal > 0
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//10. ● codCajero <> vacío
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		
		//11. ● numCaja > 0
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);	
		
		//12. ● valorChequesPropios >= 0
		UtileriaDeParametros.validarParametro(peticion.getValorChequesPropios(), "valorChequesPropios", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
		//13. ● valorChequesAjenos >= 0		
		UtileriaDeParametros.validarParametro(peticion.getValorChequesAjenos(), "valorChequesAjenos", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
		//14. ● valorChequesExt >= 0		
		UtileriaDeParametros.validarParametro(peticion.getValorChequesExt(), "valorChequesExt", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		
		}
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	// VALIDACION No. 2	
	/**
	 * M&eacutetodo para validar el arreglo de cheques obtenidos de la peticion  
	 * @param  cheques
	 * @param datos 
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 */
	private void validacionCheques(ArrayList<Cheque> cheques, DatosOperacion datos ) throws ServicioException, TipoDatoException {
		
		PagoComisionPeticion peticion = datos.obtenerObjeto("peticion",PagoComisionPeticion.class);
		
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
		 * Método para validar existencia de transacción, producto, usuario en tablas del CORE. 
		 * @param datos
		 * @throws ServicioException
		 */
		private void validarSeguridadTerminalesFinancieros(DatosOperacion datos)  throws ServicioException{
			logger.info( "[03. Invocar Función Soporte Seguridad]");
			try {
					PagoComisionPeticion peticion = datos.obtenerObjeto("peticion",PagoComisionPeticion.class);
					seguridadTerminalesFinancieros(datos);
					
					Date fechaSistema = UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
					Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistema);
												
					datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
					datos.agregarDato("codPantallaTran", datos.obtenerValor("codPantalla"));
					
			} catch (TipoDatoException|ParseException e) {
				logger.error("Error al recuperar objetos de datosOperacion");
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}	
		}
		
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/

		// VALIDACION No.4
	    /**
	 	 * Método para definir variables auxiliares
	 	 * @param datos
	 	 * @throws ServicioException
	 	 */		
		private void definirVariablesAuxiliares(DatosOperacion datos)  throws ServicioException {
			logger.info("[04. Definir Variables Auxiliares]");		
			datos.agregarDato("codConcepto",Constantes.CONCEPTO_VE);
			datos.agregarDato("codDebCre",Constantes.CREDITO);
		}
			
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		// VALIDACION No. 5
	    /**
	 	 * Método para validar la existencia del comprobante de pago de comision
	 	 * @param datos
	 	 * @throws ServicioException
	 	 */
		private void validarExistenciaComprobantePago(DatosOperacion datos)  throws ServicioException {
			logger.info("[05. Validar la existencia del comprobante de pago de comision]");		
			
			try {
				PagoComisionPeticion peticion = datos.obtenerObjeto("peticion",PagoComisionPeticion.class);
				
				Integer     codComprobante = Constantes.PAGO_COMISION_FACTURA;
				BigDecimal 	valorAplica = new BigDecimal(0);
				BigDecimal 	valorTotal  = new BigDecimal(0);
	        	
				if (UtileriaDeDatos.isEquals(peticion.getSenCreditoFiscal(), Constantes.SI)){
					codComprobante = Constantes.PAGO_COMISION_CREDITO_FISCAL;
	        	}
				
				Object[] paramsSELECT_SFBDB_IVMVE_V6 = 
						{
								Constantes.SI,
								codComprobante,
								peticion.getNumDocumentoTran()								
						};				
				logger.debug("Ejecutando sentencia SELECT_SFBDB_IVMVE_V4, parametros: " + Arrays.toString(paramsSELECT_SFBDB_IVMVE_V6));
				Map<String, Object> resultSetMap = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IVMVE_V6),paramsSELECT_SFBDB_IVMVE_V6);
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(resultSetMap);
				
				Integer 	fechaPago			= adaptador.getInteger("fechaPago");
				//BigDecimal  valorGravado		= adaptador.getBigDecimal("valorGravado");		
				//BigDecimal  valorExento			= adaptador.getBigDecimal("valorExento");		
				Integer 	codCausal			= adaptador.getInteger("codCausal");
				Integer 	codTranIVMVE		= adaptador.getInteger("codTranIVMVE");
				BigDecimal  valorIva			= adaptador.getBigDecimal("valorIva");	
				Integer 	numTran				= adaptador.getInteger("numTran");
				Integer     codOficina			= adaptador.getInteger("codOficina");
				Integer 	codProducto			= adaptador.getInteger("codProducto");
				Integer 	numCuenta			= adaptador.getInteger("numCuenta");
				Integer 	digitoVerificador	= adaptador.getInteger("digitoVerificador");
				Integer 	numeroCorrelativo	= adaptador.getInteger("numeroCorrelativo");
				Integer 	numeroDocumento	= adaptador.getInteger("numeroDocumento");
				
				Object[] paramsSELECT_SFBDB_IVMDE= 
					{
							codComprobante,
							peticion.getNumDocumentoTran()								
					};				
			logger.debug("Ejecutando sentencia SELECT_SFBDB_IVMDE, parametros: " + Arrays.toString(paramsSELECT_SFBDB_IVMDE));
			Map<String, Object> resultSetMapDet = jdbcTemplate.queryForMap(query(SELECT_IVMDE),paramsSELECT_SFBDB_IVMDE);
			AdaptadorDeMapa adaptadorDet = UtileriaDeDatos.adaptarMapa(resultSetMapDet);
				
			BigDecimal  valorGravadoTotal	   = adaptadorDet.getBigDecimal("valorGravadoTotal");		
			BigDecimal  valorExentoTotal	   = adaptadorDet.getBigDecimal("valorExentoTotal");
			
				
				if( !UtileriaDeDatos.isEquals(codCausal,200) &&
					!UtileriaDeDatos.isNull(peticion.getCuentaTransaccion()) && 
					!UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion()) &&
					!UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion(), "0000000000000")) {
					
					//Los campos se tratan como tipo String para que concuerde la comparación con el campo que viene en la petición
					if (!UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion().substring(0, 3), StringUtils.leftPad(codProducto.toString(), 3, "0"))
														||
						!UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion().substring(3, 6), StringUtils.leftPad(codOficina.toString(), 3, "0"))
														||
						!UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion().substring(6, 12),StringUtils.leftPad(numCuenta.toString(), 6, "0"))
														||
						!UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion().substring(12),digitoVerificador.toString())){
						throw new ServicioException(20108, "TIPO DE OPERACION INVALIDA {}", "- CUENTA NO ES LA REGISTRADA EN EL MÓDULO DE IVA") ;	
					}
				}
				
				if (UtileriaDeDatos.isGreater(fechaPago, new Integer(0))){
	        		throw new ServicioException(21589, "NUMERO INCORRECTO {}","- FACTURA YA ESTA PAGADA");
	        	}
	        	
	        	valorAplica = valorExentoTotal.add(valorGravadoTotal);
	        		        	      		        	
	        	try {
					if (!UtileriaDeDatos.isEquals(valorAplica,datos.obtenerBigDecimal("valorMovimiento"))){
						throw new ServicioException(21007, "CANTIDAD INCORRECTA {}","- EL MONTO NO COINCIDE");
					}
	        	
		        	valorTotal = peticion.getValorCheques().add(peticion.getValorEfectivo());
		        	
		        	if (!UtileriaDeDatos.isEquals(valorTotal, datos.obtenerBigDecimal("valorMovimiento"))){
		        		throw new ServicioException(20286, "VALOR DEL MOVIMIENTO NO ESTA CUADRADO");
		        	}
	        	} catch (ParseException e) {
					//
				}
				
				datos.agregarDato("codComprobante", codComprobante);
	        	datos.agregarDato("fechaPago", fechaPago);
				datos.agregarDato("valorGravado", valorGravadoTotal);
				datos.agregarDato("valorExento", valorExentoTotal);
				datos.agregarDato("codCausal", codCausal);
				datos.agregarDato("codTranIVMVE", codTranIVMVE);
				datos.agregarDato("valorIva", valorIva);
				datos.agregarDato("numTran", numTran);
				datos.agregarDato("codOficina", codOficina);
				datos.agregarDato("codProducto", codProducto);
				datos.agregarDato("numCuenta", numCuenta);
				datos.agregarDato("digitoVerificador", digitoVerificador);
				datos.agregarDato("numDocumento", numeroCorrelativo);	
				datos.agregarDato("senCreditoFiscal", peticion.getSenCreditoFiscal());
				datos.agregarDato("numComprobante", numeroDocumento);
			}catch (EmptyResultDataAccessException e) {
				throw new ServicioException(21589, "NUMERO INCORRECTO {}", "- PARA LA FACTURA");
			}catch (TipoDatoException e) {
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));				
			}			
		}
		
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		private void validarExistenciaSolicitoCredito(DatosOperacion datos)  throws ServicioException {
			logger.info("[6.1 Validar la existencia de la transacción en AAATR]");		
			
			try {
				  PagoComisionPeticion peticion = datos.obtenerObjeto("peticion",PagoComisionPeticion.class);

				Object[] paramsSELECT_SFBDB_PPASO= {
						Integer.valueOf(peticion.getCuentaTransaccion().substring(0, 3)),
						Integer.valueOf(peticion.getCuentaTransaccion().substring(3, 6)),
						Integer.valueOf(peticion.getCuentaTransaccion().substring(6, 12))
				};
				    
				Map<String, Object> mapPPASO = null;
				Integer cantidadRegistros = null;
				
			    logger.info("[06.1 Ejecutando sentencia SELECT_SFBDB_PPASO, parametros: " + Arrays.toString(paramsSELECT_SFBDB_PPASO));
				//Ejecución de sentencia SELECT_LINC_SFBDB_AAATR_V10 y recuperación de datos
			     mapPPASO = jdbcTemplate.queryForMap(query(SELECT_SFBDB_PPASO), paramsSELECT_SFBDB_PPASO);
				 cantidadRegistros = ((BigDecimal) mapPPASO.get("cantidadRegistros")).intValue();
				
				 if(UtileriaDeDatos.isEquals(cantidadRegistros, new Integer(0))) {
		        		throw new ServicioException(4161, "NO EXISTE LA SOLICITUD DE CREDITO");
				 }
			
			}catch (EmptyResultDataAccessException e) {
				//Se espera que venga vacío por lo que no debe detener la transacción
			}
			catch (TipoDatoException e) {
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}			
		}
		
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
		// VALIDACION No. 6.2
	    /**
	 	 * Método para validar la cuenta ingresada
	 	 * @param datos
	 	 * @throws ServicioException
	 	 */
		private void recuperarDatosCuenta(DatosOperacion datos) throws ServicioException {
			try {
				logger.info("[6.2 Recuperar datos de la cuenta ingresada]");		

				PagoComisionPeticion peticion;
				peticion = datos.obtenerObjeto("peticion", PagoComisionPeticion.class);
				Cliente cliente = null;
				Integer codOficinaRecuperado = 0;
				Integer codProductoRecuperado = 0;
				Integer numCuentaRecuperado = 0;
				Integer digitoVerificadorRecuperado = 0;
				datos.agregarDato("codCliente", " ");
				
				if(!UtileriaDeDatos.isNull(peticion.getCuentaTransaccion()) && 
					!UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion()) &&
					!UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion(), "0000000000000")) {
					
					Integer codProducto = Integer.parseInt((peticion.getCuentaTransaccion().toString().substring(0, 3)));
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
						datos.agregarDato("cliente", cliente);
						datos.agregarDato("codCliente",cliente.getCodCliente());
						break;
		
					case 2:
		
						CuentaAhorro pca = recuperarDatosCuentaAhorro(peticion.getCuentaTransaccion());
						cliente = recuperarDatosCliente(pca.getCodCliente());
						codOficinaRecuperado = pca.getCodOficina();        
						codProductoRecuperado =  pca.getCodProducto();                
						numCuentaRecuperado   =  pca.getNumCuenta();         
						digitoVerificadorRecuperado = pca.getDigitoVerificador();
						datos.agregarDato("pca", pca);
						datos.agregarDato("cliente", cliente);
						datos.agregarDato("codCliente",cliente.getCodCliente());						
						break;
		
					case 4:
		
						Certificado pce = recuperarDatosCuentaCertificado(peticion.getCuentaTransaccion());
						cliente = recuperarDatosCliente(pce.getCodCliente());
						codOficinaRecuperado = pce.getCodOficina();        
						codProductoRecuperado =  pce.getCodProducto();                
						numCuentaRecuperado   =  pce.getNumCuenta();         
						digitoVerificadorRecuperado = pce.getDigitoVerificador();
						datos.agregarDato("pce", pce);
						datos.agregarDato("cliente", cliente);
						datos.agregarDato("codCliente",cliente.getCodCliente());
						break;
		
					case 6:
		
						CuentaPrestamo pcp = recuperarDatosCuentaPrestamo(peticion.getCuentaTransaccion());
						cliente = recuperarDatosCliente(pcp.getCodCliente());
						codOficinaRecuperado = pcp.getCodOficina();        
						codProductoRecuperado =  pcp.getCodProducto();                
						numCuentaRecuperado   =  pcp.getNumCuenta();         
						digitoVerificadorRecuperado = pcp.getDigitoVerificador();
						datos.agregarDato("pcp", pcp);
						datos.agregarDato("cliente", cliente);
						datos.agregarDato("codCliente",cliente.getCodCliente());
						break;
					}
				}
				
				datos.agregarDato("codOficinaRecuperado", codOficinaRecuperado);
				datos.agregarDato("codProductoRecuperado", codProductoRecuperado);
				datos.agregarDato("numCuentaRecuperado", numCuentaRecuperado);
				datos.agregarDato("digitoVerificadorRecuperado", digitoVerificadorRecuperado);
				
			} catch (TipoDatoException e) {
				//
			}			
		}
		
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	 	
		// VALIDACION No. 7
	    /**
	 	 * Método para validar la existencia del moivmiento en AAATR y que no permita aplicar más de una vez, dado a que todos los datos 
	 	 * del movimiento los recupera del módulo de IVA; así mismo el numTran es permanente, no es un correlativo que se va generando
	 	 * @param datos
	 	 * @throws ServicioException
	 	 */
		private void validarExistenciaTransaccionAAATR(DatosOperacion datos)  throws ServicioException {
			logger.info("[07. Validar la existencia de la transacción en AAATR]");		
			
			try {
				PagoComisionPeticion peticion = datos.obtenerObjeto("peticion",PagoComisionPeticion.class);
				
				MathContext m = new MathContext(3); // 4 precision
				
			
				Object[] paramsSELECT_SFBDB_AAATR_V7 = {
				    		datos.obtenerValor("fechaRelativa"),
				    		peticion.getCodOficinaTran(),
				    		peticion.getCodTerminal(),
				    		datos.obtenerValor("numTran"),
				    		datos.obtenerValor("codTranIVMVE"),
				    		datos.obtenerValor("codCausal"),
				    		peticion.getNumDocumentoTran(),
				    		peticion.getValorMovimiento().round(m),
				    		Constantes.SI
				};
				    
				Map<String, Object> mapAAATR = null;
				Integer cantidadRegistros = null;
				
			    logger.debug("[06.1 Ejecutando sentencia SELECT_SFBDB_AAATR_V5, parametros: " + Arrays.toString(paramsSELECT_SFBDB_AAATR_V7));
				//Ejecución de sentencia SELECT_LINC_SFBDB_AAATR_V10 y recuperación de datos
				mapAAATR = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAATR_V7), paramsSELECT_SFBDB_AAATR_V7);
				cantidadRegistros = ((BigDecimal) mapAAATR.get("cantidadRegistros")).intValue();
				
				if(UtileriaDeDatos.isGreater(cantidadRegistros, new Integer(0))) {
	        		throw new ServicioException(20109, "SOLO ES PERMITIDO UN TIPO DE OPERACION AL DIA, DEBE REVERSAR");
				}
			
			}catch (EmptyResultDataAccessException e) {
				//Se espera que venga vacío por lo que no debe detener la transacción
			}
			catch (TipoDatoException e) {
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}			
		}
		
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
		// VALIDACION No. 8
		
		/**
		 * Método para la actualización de los datos del cupón a cancelar y su respectivo registro en AAAATR
		 * @param datos
		 * @throws ServicioException
		 */
		private void registrarDatosAAATR(DatosOperacion datos) throws ServicioException{
			logger.info("[08. Registrar Transacción en tabla AAATR]");
			try {
				
				PagoComisionPeticion peticion = datos.obtenerObjeto("peticion", PagoComisionPeticion.class);
				Cliente cliente = datos.obtenerObjeto("cliente", Cliente.class);
				String codCliente = " ";
				if (!UtileriaDeDatos.isNull(datos.obtenerObjeto("cliente", Cliente.class))) {
					codCliente = cliente.getCodCliente();
				}
				datos.agregarDato("codigoCliente", codCliente);
				
				//Los siguientes if se realizan ya que los datos recuperados de la tabla de IVA devuelven CERO y dan error al insertar en AAATR
				//Se verifica si el codTran recuperado de la tabla de IVA es igual a cero, si es así entonces deja el codTran recibido en la petición
				//Si no, deja el recuperado de la tabla
				if (UtileriaDeDatos.isEquals(datos.obtenerInteger("codTranIVMVE"), new Integer(0))) {
					datos.agregarDato("codTran", peticion.getCodTran());					
				}else {
					datos.agregarDato("codTran", datos.obtenerInteger("codTranIVMVE"));
				}				
				
				//Se verifica si el numTran recuperado de la tabla de IVA es igual a cero, si es así entonces genera un numTran
				//Si no, deja el recuperado de la tabla
				if (UtileriaDeDatos.isEquals(datos.obtenerInteger("numTran"), new Integer(0))) {
					Integer numTranComision = (Integer) jdbcTemplate.queryForObject(query(SELECT_FNC_CORREL_CANAL_V8), Integer.class,Constantes.VENTANILLA);
					datos.agregarDato("numTran", numTranComision);						
				}
				
				//logger.debug("SEÑAL POSTEO" + datos.obtenerInteger("senPosteo"));
					
				//datos.agregarDato("codCausal", peticion.getCodCausal());// ya agregado
				//datos.agregarDato("codConcepto", datos.obtenerValor("codConcepto"));//ya agregado		
				//datos.agregarDato("codOficina", datos.obtenerInteger("codOficina"));//ya agregado			
				//datos.agregarDato("codProducto", datos.obtenerInteger("codProducto"));//ya agregado
				//datos.agregarDato("numCuenta", datos.obtenerInteger("numCuenta"));//ya agregado
				//datos.agregarDato("digitoVerificador", datos.obtenerInteger("digitoVerificador"));//ya agregado
				//datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());//ya agregado			
	   		    datos.agregarDato("codTerminalTran", peticion.getCodTerminal());//ya agregado		
	   		    //datos.agregarDato("fechaRelativa", datos.obtenerValor("fechaRelativa"));//ya agregado			
				datos.agregarDato("horaTran", datos.obtenerValor("horaSistema"));						
				//datos.agregarDato("numTran", datos.obtenerValor("numTran"));//ya agregado			
				datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());//ya agregado						
				//datos.agregarDato("codCompania", datos.obtenerValor("codCompania"));//ya agregado
				//datos.agregarDato("codMoneda", datos.obtenerValor("codMoneda"));//ya agregado
				//datos.agregarDato("numCaja",peticion.getNumCaja());//ya agregado
				datos.agregarDato("montoIVA", null ); 
				//datos.agregarDato("codTran", datos.obtenerValor("codTran"));ya agregado y validado			
				//datos.agregarDato("codCajero", peticion.getCodCajero());//ya agregado
				//datos.agregarDato("codDebCre", datos.obtenerValor("codDebCre"));//ya agregado									
				datos.agregarDato("numSecuenciaCupon", new Integer(0));
				datos.agregarDato("valorImpuestoVenta", datos.obtenerValor("valorIva"));						
				datos.agregarDato("codSectorEconomico",0);
				datos.agregarDato("numDiasAtras", null);						
				//datos.agregarDato("fechaSistema", datos.obtenerValor("fechaSistema"));
				datos.agregarDato("fechaTran", datos.obtenerValor("fechaSistema"));			
				datos.agregarDato("numDocumentoReversa", null);
				datos.agregarDato("saldoAnterior", null);						
				datos.agregarDato("senAJATR", Constantes.NO);									
				datos.agregarDato("senAutorizacion", Constantes.NO);			
				datos.agregarDato("senReversa", Constantes.NO);						
				//datos.agregarDato("senSupervisor", peticion.getSenSupervisor());//ya agregado				
				datos.agregarDato("senWANG", null);			
				datos.agregarDato("senDiaAnterior", Constantes.NO);	
				datos.agregarDato("senImpCaja", Constantes.SI);						
				datos.agregarDato("senPosteo", Constantes.NO);
				datos.agregarDato("valorAnterior", null);									
				datos.agregarDato("valorCompra", BigDecimal.ONE);
				//datos.agregarDato("valorMovimiento",peticion.getValorMovimiento());ya agregado	
				datos.agregarDato("valorCheque",peticion.getValorCheques());						
				datos.agregarDato("valorVenta", BigDecimal.ONE);
				datos.agregarDato("numDocumentoTran2", null);						
				datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());			
				datos.agregarDato("valorChequesExt", peticion.getValorChequesExt());
				datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());			
				datos.agregarDato("descripcionTran", " ");						
				datos.agregarDato("codBancoTransf", null);			
				datos.agregarDato("codPaisTransf", null);			
				datos.agregarDato("senACRM", Constantes.SI);
				datos.agregarDato("codCliente", codCliente);						
				datos.agregarDato("valorImpuesto", null);						
				datos.agregarDato("tipDocumentoCliente", 0);
				datos.agregarDato("numDocumentoCliente", 0);			
				datos.agregarDato("numDocumentoImp", 0);						
				datos.agregarDato("codSubCausal",0);
				
				registrarTransaccionAAATR(datos);   
					
			} catch (TipoDatoException e) {
				logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}
			
		}		

		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
		// VALIDACION No. 9
		/**
		 * Método para actualizar datos en el maestro de Facturas y  gastos de préstamos
		 * @param datos
		 * @throws ServicioException
		 */
		private void actualizarDatosMaestroFactura(DatosOperacion datos) throws ServicioException {
			logger.info("[09. Actualizar datos en el maestro de Facturas y  gastos de préstamos.]");
			
			Integer numDoc = new Integer(0);

			try {
				PagoComisionPeticion peticion = datos.obtenerObjeto("peticion", PagoComisionPeticion.class);
				
				if(((Integer) datos.obtenerValor("codProducto")) < 600
						                             &&
				   ((Integer) datos.obtenerValor("codProducto")) > 699) {
					
					
					numDoc=datos.obtenerInteger("numComprobante");
				}
				else 
				{
					numDoc=datos.obtenerInteger("numDocumento");	
				}
				
								
				Object[] paramsUPDATE_SFBDB_IVMVE_V9_1 = {
					    datos.obtenerValor("numCaja"),
						datos.obtenerValor("codTerminal"),
						datos.obtenerValor("codOficinaTran"),
						datos.obtenerValor("fechaSistema"),
						datos.obtenerValor("numDocumento"),
						datos.obtenerValor("codTran"),
						datos.obtenerValor("numTran"),
						Constantes.SI,
						datos.obtenerValor("codComprobante"),
						numDoc					
				};			
				
				logger.debug("Ejecutando sentencia UPDATE_SFBDB_IVMVE_V6_1, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_IVMVE_V9_1));
				ejecutarSentencia(query(UPDATE_SFBDB_IVMVE_V9_1), paramsUPDATE_SFBDB_IVMVE_V9_1);
				
				if(!UtileriaDeDatos.isNull(peticion.getCuentaTransaccion()) && 
				   !UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion()) &&
				   !UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion(), "0000000000000")) {
					Object[] paramsSELECT_SFBDB_AAMPR_V9_2 = 
						{ 
							Integer.valueOf(peticion.getCuentaTransaccion().substring(0, 3))	//datos.obtenerValor("codProducto")
						};
					
					logger.debug("Ejecutando sentencia SELECT_SFBDB_AAMPR_V6_2, parametros: " + Arrays.toString(paramsSELECT_SFBDB_AAMPR_V9_2));
					Integer codConceptoAAMPR = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMPR_V9_2), Integer.class,	paramsSELECT_SFBDB_AAMPR_V9_2);
					
					if(UtileriaDeDatos.isEquals(codConceptoAAMPR, Constantes.CONCEPTO_PP)){
						
						Object[] paramsUPDATE_SFBDB_PPRGP_V9_4 = {
                                Integer.valueOf(peticion.getCuentaTransaccion().substring(3, 6)),
								Integer.valueOf(peticion.getCuentaTransaccion().substring(0, 3)),
								Integer.valueOf(peticion.getCuentaTransaccion().substring(6, 12)),
								Constantes.SI,
							//	datos.obtenerValor("codComprobante"),
								datos.obtenerValor("numComprobante")					
						};	
						
						Map<String, Object> map = null;
						Integer cantidadRegistros = null;
						
						
						map = jdbcTemplate.queryForMap(query(SELECT_SFBDB_PPRGP_V9_5), paramsUPDATE_SFBDB_PPRGP_V9_4);
						cantidadRegistros = ((BigDecimal) map.get("cantidadRegistros")).intValue();
						
						
						 
						if(UtileriaDeDatos.isGreater(cantidadRegistros, new Integer(0))) {
			        		throw new ServicioException(20001, " GASTO DE PRESTAMO YA PAGADO"," GASTO DE PRESTAMO YA PAGADO");
						}
					    
						//Ejecución de sentencia SELECT_LINC_SFBDB_AAATR_V10 y recuperación de datos
						map = jdbcTemplate.queryForMap(query(SELECT_SFBDB_PPRGP_V9_4), paramsUPDATE_SFBDB_PPRGP_V9_4);
						cantidadRegistros = ((BigDecimal) map.get("cantidadRegistros")).intValue();
						
						
						 
						if(UtileriaDeDatos.isEquals(cantidadRegistros, new Integer(0))) {
			        		throw new ServicioException(21589, "NO EXISTE GASTO ASOCIADO A LA FACTURA"+datos.obtenerValor("numComprobante"),"NO EXISTE GASTO ASOCIADO A LA FACTURA"+datos.obtenerValor("numComprobante"));
						}
						
						
						Object[] paramsUPDATE_SFBDB_PPRGP_V9_3 = {
								Constantes.PP_ESTADO_GASTO_CANCELADO,
                                Integer.valueOf(peticion.getCuentaTransaccion().substring(3, 6)),
								Integer.valueOf(peticion.getCuentaTransaccion().substring(0, 3)),
								Integer.valueOf(peticion.getCuentaTransaccion().substring(6, 12)),
								Constantes.SI,
							//	datos.obtenerValor("codComprobante"),
								datos.obtenerValor("numComprobante")					
						};	
						
						logger.debug("Ejecutando sentencia UPDATE_SFBDB_PPRGP_V6_3, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_PPRGP_V9_3));
						ejecutarSentencia(query(UPDATE_SFBDB_PPRGP_V9_3), paramsUPDATE_SFBDB_PPRGP_V9_3);
					}	
				}	
			}catch (TipoDatoException e) {
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));	
			}
		}	
		
		
		/**
		 * M&eacutetodo para procesar l&oacutegica de pagos de cheques propios y retenciones
		 * @param  datos
		 * @throws ServicioException
		 * @throws TipoDatoException 
		 */
		private void procesarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {
			logger.info("[10. Procesar Cheques.]");

			PagoComisionPeticion peticion = datos.obtenerObjeto("peticion", PagoComisionPeticion.class);
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
						
						datos.agregarDato("tipDocumentoPersona",new Integer(0));
						datos.agregarDato("numDocumentoPersona"," " );
						datos.agregarDato("nombrePersona", " ");

						pagosCheques(datos);
						chk.setNumTran(datos.obtenerInteger("numTran"));
						chk.setCodCajero(datos.obtenerString("codCajero"));
						chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
						chk.setNomOficina(datos.obtenerString("nomOficina"));
					}
					//Para pago de Comisiciones, no se retienen los cheques ajenos.
					else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 5)){ 						
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
			datos.agregarDato("codCliente", datos.obtenerString("codigoCliente"));
			datos.agregarDato("codPantalla", datos.obtenerValor("codPantallaTran"));
			
		}
}