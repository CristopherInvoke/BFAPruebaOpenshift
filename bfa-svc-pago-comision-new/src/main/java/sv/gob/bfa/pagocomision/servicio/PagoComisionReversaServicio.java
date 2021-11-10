package sv.gob.bfa.pagocomision.servicio;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.pagocomision.model.PagoComisionReversaPeticion;
import sv.gob.bfa.pagocomision.model.PagoComisionReversaRespuesta;

/**
 * @author Claudia Gonzalez
 * @version 10/1/2020
 * La lógica para Pago de Comision (AJ209), se ha creado a partir del documento Disenio_Pago_Comision_AJ209
 * Permite realizar pago de comisión, como un cargo del Banco hacia un cliente.
 * Se crea un objeto principal llamado "datos", el cual se va llenando con todas las propiedades y objetos recuperados de cada actividad
 * o validación realizada, para ello se utilizan 2 funciones principales, de acuerdo a la necesidad: agregarDato, agregarPropiedades.
 * Se crea el objeto "peticion" El cual se crea a partir de la clase PagoComisionReversaPeticion, que son los campos que llenará la aplicación cliente.
 * Se crea el objeto "respuesta":El cual se crea a partir de la clase PagoComisionReversaRespuesta, que son los campos que se enviarán a la aplicación cliente
 * Así mismo se declaran como constantes todas las sentencias SQL a ser consumidas tanto para CONSULTAS como para las ACTUALIZACIONES.
 * Se consumen métodos definidos en la clase Servicio (Ubicada en Comunes), como por ejemplo la ejecución de las consultas y actualizaciones 
 * */
public class PagoComisionReversaServicio extends Servicio{
	
	private static final String SELECT_SFBDB_IVMVE_V4 = ""
			+" SELECT NVL(bvaafecta, 0) AS valorGravado,"
			+"        NVL(bvaexento, 0) AS valorExento,"
			+"        NVL(bco_causa, 0) AS codCausal,"
			+"        NVL(bco_trans, 0) AS codTranIVMVE,"
			+"        NVL(bcu_produ, 0) AS codProducto,"
			+"        NVL(bcunumcue, 0) AS numCuenta,"
			+"        NVL(bnudoctra, 0) AS numeroDocumento"   
			+"  FROM linc.sfbdb_ivmve@DBLINK@"
			+" WHERE bcodonant = ?"
			+"   AND bcofacfis = ?"
			+"   AND bnufacfis = ?"
			;
	
	private final static String SELECT_SFBDB_AAATR_V5_1 =""
			+ "SELECT glb_dtime as glbDtimeAAATR"
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
	
	private final static String UPDATE_SFBDB_AAATR_V5_2 = ""
			+ " UPDATE linc.sfbdb_aaatr@DBLINK@  "
			+ "	   SET tse_rever = ? "
			+ "  WHERE glb_dtime = ? "
			;
	
	private static final String UPDATE_SFBDB_IVMVE_V6_1= 
			"UPDATE LINC.SFBDB_IVMVE@DBLINK@" + 
			"   SET BNU_CAJA  = ?," + 
			" 		BCO_TEPAG = ?," + 
			" 		BCO_OFPAG = ?," + 
			"       BFE_PAGO  = ?" +
			//" 		BNUDOCTRA = ?" + 
			" WHERE BCODONANT = ?" + 
			"   AND BCOFACFIS = ?" + 
			"   AND BNUFACFIS = ?"
			;

		
	
	private final static String SELECT_SFBDB_AAMPR_V6_2 = ""
			+ " SELECT NVL(aco_conce, 0) AS codConceptoAAMPR"
			+ "   FROM linc.sfbdb_aampr@DBLINK@  " 
			+ "  WHERE aco_produ = ?"
			;	
	
	private static final String UPDATE_SFBDB_PPRGP_V6_3= 
			"UPDATE LINC.SFBDB_PPRGP@DBLINK@" + 
			"   SET pco_estad = ?" + 
			" WHERE pcu_ofici = ?" + 
			" 	AND	pcu_produ = ?" + 
			" 	AND	pcunumcue = ?" + 
			" 	AND psecobant = ?" + 
		//	" 	AND	pcunumcue = ?" + 
			" 	AND	pnucompro = ?" 
			;
	
	
	private static final String SELECT_IVMDE="SELECT SUM(BVAAFECTA) valorGravadoTotal,SUM(BVAEXENTO) valorExentoTotal from linc.sfbdb_IVMDE@DBLINK@ where bcofacfis=? and bnufacfis=?";
	

	Logger logger = LoggerFactory.getLogger(PagoComisionReversaServicio.class);
	
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
		
		logger.debug("Cast de objeto de dominio -> PagoComisionReversaPeticion ");
		PagoComisionReversaPeticion peticion = (PagoComisionReversaPeticion) objetoDom;
		
		//Una vez creada la petición, se agrega dicho objeto al objeto principal "datos"
		datos.agregarDato("peticion", peticion);
		datos.agregarPropiedadesDeObjeto(peticion);
		
		//Se realiza el llamado de cada metodo que se traduce practicamente a cada actividad descrita en el documento de diseño
		try {
			
			// VALIDACION No. 1
			// 01. Validar Parámetros
			validacionInicial(peticion);

			// VALIDACION No. 2
			// 02. Validar Parámetros
			validacionCheques((ArrayList<Cheque>) peticion.getCheques());
			
			// VALIDACION No. 3
			// 03. Invocar Función Soporte Seguridad		
			validarSeguridadTerminalesFinancieros(datos);

			// VALIDACION No. 4
			// 04. Registrar Transacción en tabla AAATR
			validarExistenciaComprobantePago(datos);
			
			// VALIDACION No. 5
			// 05. Registrar Transacción en tabla AAATR
			validarTransaccionReversarnAAATR(datos);

			// VALIDACION No. 6
			// 06. Registrar Transacción en tabla AAATR
			actualizarDatosMaestroFactura(datos);
			
			// VALIDACION No. 7
			// 07. Registrar Transacción en tabla AAATR			
			ReversarChequesPropiosRetencionesGerencia(datos);
			
			logger.debug("Preparando objeto de respuesta ...");
			
			PagoComisionReversaRespuesta respuesta = new PagoComisionReversaRespuesta(); 
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			if(logger.isDebugEnabled()) {
				logger.debug("RESPUESTA PagoComisionReversa : {} ", respuesta);
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
	private void validacionInicial(PagoComisionReversaPeticion peticion) throws ServicioException{
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
		
		//8. ● codOficinaTran > 0
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//9. ● codTerminal > 0
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		
		//10. ● codCajero <> vacío
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		
		//11. ● numCaja > 0
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);	
		
		//12. ● numReversa > 0
		UtileriaDeParametros.validarParametro(peticion.getNumReversa(), "numReversa", TipoValidacion.ENTERO_MAYOR_CERO);
		
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
		 * Método para validar existencia de transacción, producto, usuario en tablas del CORE. 
		 * @param datos
		 * @throws ServicioException
		 */
		private void validarSeguridadTerminalesFinancieros(DatosOperacion datos)  throws ServicioException{
			logger.info( "[03. Invocar Función Soporte Seguridad]");
			try {
				PagoComisionReversaPeticion peticion = datos.obtenerObjeto("peticion",PagoComisionReversaPeticion.class);
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
	 	
		// VALIDACION No. 4
	    /**
	 	 * Método para validar la existencia del comprobante de pago de comision
	 	 * @param datos
	 	 * @throws ServicioException
	 	 */
		private void validarExistenciaComprobantePago(DatosOperacion datos)  throws ServicioException {
			logger.info("[04. Validar la existencia del comprobante de pago de comision]");		
			
			try {
				PagoComisionReversaPeticion peticion = datos.obtenerObjeto("peticion",PagoComisionReversaPeticion.class);
				
				Integer     codComprobante = Constantes.PAGO_COMISION_FACTURA;
				BigDecimal 	valorAplica = new BigDecimal(0);
				BigDecimal 	valorTotal  = new BigDecimal(0);
				
				if (UtileriaDeDatos.isEquals(datos.obtenerInteger("senCreditoFiscal"), Constantes.SI)){
					codComprobante = Constantes.PAGO_COMISION_CREDITO_FISCAL;
	        	}
				
				Object[] paramsSELECT_SFBDB_IVMVE_V4 = 
						{
								Constantes.SI,
								codComprobante,
								(Integer) datos.obtenerValor("numDocumentoTran")								
						};				
				logger.debug("Ejecutando sentencia SELECT_SFBDB_IVMVE_V4, parametros: " + Arrays.toString(paramsSELECT_SFBDB_IVMVE_V4));
				Map<String, Object> resultSetMap = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IVMVE_V4),paramsSELECT_SFBDB_IVMVE_V4);
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(resultSetMap);
				
				//BigDecimal  valorGravado		= adaptador.getBigDecimal("valorGravado");		
				//BigDecimal  valorExento			= adaptador.getBigDecimal("valorExento");		
				Integer 	codCausal			= adaptador.getInteger("codCausal");
				Integer 	codTranIVMVE		= adaptador.getInteger("codTranIVMVE");
				Integer 	codProducto			= adaptador.getInteger("codProducto");
				Integer 	numCuenta			= adaptador.getInteger("numCuenta");
				Integer 	numeroDocumento		= adaptador.getInteger("numeroDocumento");
				
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
				
	        	valorAplica = valorExentoTotal.add(valorGravadoTotal);
	        	try {
					logger.debug("valorAplica" + valorAplica + "valorExento" + valorExentoTotal + "valorMovimiento" + datos.obtenerBigDecimal("valorMovimiento"));
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        		        	      		        	
	        	try {
					if (!UtileriaDeDatos.isEquals(valorAplica,datos.obtenerBigDecimal("valorMovimiento"))){
						throw new ServicioException(20007, "CANTIDAD INCORRECTA {}", "- EL MONTO NO COINCIDE");
					}
	        	
		        	valorTotal = peticion.getValorCheques().add(peticion.getValorEfectivo());
		        	
		        	if (!UtileriaDeDatos.isEquals(valorTotal, datos.obtenerBigDecimal("valorMovimiento"))){
		        		throw new ServicioException(20286, "VALOR DEL MOVIMIENTO NO ESTA CUADRADO");
		        	}
	        	} catch (ParseException e) {
					//
				}
				
				datos.agregarDato("codComprobante", codComprobante);
				datos.agregarDato("codCausalIVMVE", codCausal);
				datos.agregarDato("codTranIVMVE", codTranIVMVE);
				datos.agregarDato("codProducto", codProducto);
				datos.agregarDato("numCuenta", numCuenta);
				datos.agregarDato("numeroDocumento", numeroDocumento);
			
			}catch (EmptyResultDataAccessException e) {
				throw new ServicioException(21589, "NUMERO INCORRECTO {}", "- PARA LA FACTURA");
			}catch (TipoDatoException e) {
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}			
		}
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
		// VALIDACION No. 5		
		/**
		 * Método para validar transacción a reversar dentro de AAAATR
		 * @param datos
		 * @throws ServicioException
		 */
		private void validarTransaccionReversarnAAATR(DatosOperacion datos) throws ServicioException{
			logger.info("05. Validar transacción a reversar en tabla AAATR]");
			try {
				
      			PagoComisionReversaPeticion peticion = datos.obtenerObjeto("peticion", PagoComisionReversaPeticion.class);
				MathContext m = new MathContext(3); // 2 precision
				
	    		logger.debug( "TRANSACCION --------------------       " + datos.obtenerValor("codTranIVMVE"));
	    		logger.debug( "CAUSAL: ------------------------          " + datos.obtenerValor("codCausalIVMVE"));
	    		
	    		if (UtileriaDeDatos.isEquals(datos.obtenerInteger("codTranIVMVE"), new Integer(0))) {
					datos.agregarDato("codTran", peticion.getCodTran());					
				}else {
					datos.agregarDato("codTran", datos.obtenerInteger("codTranIVMVE"));
				}
				
			    Object[] paramsSELECT_SFBDB_AAATR_V5_1 = {
			    		datos.obtenerValor("fechaRelativa"),
			    		peticion.getCodOficinaTran(),
			    		peticion.getCodTerminal(),
			    		peticion.getNumReversa(),
			    		datos.obtenerValor("codTran"),
			    		datos.obtenerValor("codCausalIVMVE"),
			    		peticion.getNumDocumentoTran(),
			    		peticion.getValorMovimiento().round(m),
			    		Constantes.SI
			    };
			    
				Map<String, Object> mapAAATR = null;
				Long glbDtimeAAATR = null;
				
				try {
				    logger.info("[05.1 Ejecutando sentencia SELECT_SFBDB_AAATR_V4, parametros: " + Arrays.toString(paramsSELECT_SFBDB_AAATR_V5_1));

				    mapAAATR = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAATR_V5_1), paramsSELECT_SFBDB_AAATR_V5_1);
					glbDtimeAAATR = ((BigDecimal)mapAAATR.get("glbDtimeAAATR")).longValue();

					//Se agrega dato recuperado al objeto "datos"
					datos.agregarDato("glbDtimeAAATR", glbDtimeAAATR);
					datos.agregarDato("glbDtime", glbDtimeAAATR);
					
					
				} catch (EmptyResultDataAccessException erdae) {
					logger.error("No existe la operacion realizada...");
					throw new ServicioException(20212, "TRANSACCION NO APARECE EN BASE DE DATOS");
				}
				
			    Object[] paramsUPDATE_SFBDB_AAATR_V5_2 = {
			    		Constantes.SI,		    		
			    		glbDtimeAAATR
			    };
			    
				//Ejecución de sentencia UPDATE_SFBDB_AAATR_V10_2 y recuperación de datos
			    logger.debug("[05.2 Ejecutando sentencia UPDATE_SFBDB_AAATR_V4_2, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_AAATR_V5_2));
				ejecutarSentencia(query(UPDATE_SFBDB_AAATR_V5_2), paramsUPDATE_SFBDB_AAATR_V5_2);
				
				datos.agregarDato("codTerminalTran",datos.obtenerValor("codTerminal"));
				datos.agregarDato("numTran",datos.obtenerValor("numReversa"));

				
				//Se actualizan los perfiles de AAATR
				logger.debug("[05.3 Se invoca la función de actualización de perfiles de transacción");
				actualizarPerfilesTransaccionAAATR(datos);	
			
			} catch (TipoDatoException e) {
				logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
				throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
			}
			
		}		

		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		/*----------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		
		// VALIDACION No. 6
		/**
		 * Método para actualizar datos en el maestro de Facturas y  gastos de préstamos
		 * @param datos
		 * @throws ServicioException
		 */
		private void actualizarDatosMaestroFactura(DatosOperacion datos) throws ServicioException {
			logger.info("[06. Actualizar datos en el maestro de Facturas y  gastos de préstamos.]");
			
			try {	
				PagoComisionReversaPeticion peticion = datos.obtenerObjeto("peticion", PagoComisionReversaPeticion.class);
	
				Object[] paramsUPDATE_SFBDB_IVMVE_V6_1 = {
						new Integer(0),
						new Integer(0),
						new Integer(0),
						new Integer(0),
						//new Integer(0),
						Constantes.SI,
						datos.obtenerValor("codComprobante"),
						datos.obtenerValor("numDocumentoTran")					
				};			
				
				logger.debug("Ejecutando sentencia UPDATE_SFBDB_IVMVE_V6_1, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_IVMVE_V6_1));
				ejecutarSentencia(query(UPDATE_SFBDB_IVMVE_V6_1), paramsUPDATE_SFBDB_IVMVE_V6_1);
				
				if(!UtileriaDeDatos.isNull(peticion.getCuentaTransaccion()) && 
						!UtileriaDeDatos.isBlank(peticion.getCuentaTransaccion()) &&
						!UtileriaDeDatos.isEquals(peticion.getCuentaTransaccion(), "0000000000000")) {
					
					Object[] paramsSELECT_SFBDB_AAMPR_V6_2 = 
						{ 
							Integer.valueOf(peticion.getCuentaTransaccion().replaceAll("-", "").substring(0, 3))
						};
					
					logger.debug("Ejecutando sentencia SELECT_SFBDB_AAMPR_V6_2, parametros: " + Arrays.toString(paramsSELECT_SFBDB_AAMPR_V6_2));
					Integer codConceptoAAMPR = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMPR_V6_2), Integer.class,	paramsSELECT_SFBDB_AAMPR_V6_2);
					logger.debug("TnuDOTRA A REVERAR:"+datos.obtenerValor("numeroDocumento"));
					if(UtileriaDeDatos.isEquals(codConceptoAAMPR, Constantes.CONCEPTO_PP)){
					
						Object[] paramsUPDATE_SFBDB_PPRGP_V6_3 = {
								Constantes.PP_ESTADO_GASTO_FACTURADO,
								 Integer.valueOf(peticion.getCuentaTransaccion().replaceAll("-", "").substring(3, 6)),
							     Integer.valueOf(peticion.getCuentaTransaccion().replaceAll("-", "").substring(0, 3)),
							     Integer.valueOf(peticion.getCuentaTransaccion().replaceAll("-", "").substring(6, 12)),
								Constantes.SI,
							//	datos.obtenerValor("codComprobante"),
								datos.obtenerValor("numeroDocumento")						
						};			
						
						logger.debug("Ejecutando sentencia UPDATE_SFBDB_PPRGP_V6_3, parametros: " + Arrays.toString(paramsUPDATE_SFBDB_PPRGP_V6_3));
						ejecutarSentencia(query(UPDATE_SFBDB_PPRGP_V6_3), paramsUPDATE_SFBDB_PPRGP_V6_3);
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
		private void ReversarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {

			PagoComisionReversaPeticion peticion = datos.obtenerObjeto("peticion", PagoComisionReversaPeticion.class);
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
					//Para pago de Comisiciones, no se retienen los cheques ajenos.
					else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 5)){ 						
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
			datos.agregarDato("codPantalla", datos.obtenerValor("codPantallaTran"));
		}
		
}