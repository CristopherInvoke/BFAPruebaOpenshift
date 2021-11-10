package sv.gob.bfa.pago.gastos.ajenos.prestamo.servicio;


import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.CuentaPrestamo;
import sv.gob.bfa.core.model.Peticion;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;
import sv.gob.bfa.pago.gastos.ajenos.prestamo.model.ReversaPagoGastosAjenosPrestamoPeticion;
import sv.gob.bfa.pago.gastos.ajenos.prestamo.model.ReversaPagoGastosAjenosPrestamoRespuesta;


/**
 * Clase contiene logica del negocio correspondiente a Reversa deposito mixto de cuente corriente
 */
public class ReversaPagoGastosAjenosPrestamoServicio extends Servicio{
	private static String SELECT_SFBDB_AAMVT =
			" SELECT ATA_VENCI/100 AS tasaIVA" + 
			" FROM LINC.SFBDB_AAMVT@DBLINK@" + 
			" WHERE ACOTASREF = ?" + 
			" AND AFETASREF <= ?" + 
			" AND ROWNUM <= ?"; 
	public static final Integer PP_ESTADO_VENCIDO 		 			= 2;
	public static final Integer PP_ESTADO_CASTIGADO		 			= 7;
	
	
	private static String  SELECT_SFBDB_AAMPR = "SELECT DCO_MONED as codMoneda" + 
			"	FROM LINC.SFBDB_AAMPR@DBLINK@" +
			"	WHERE ACO_PRODU = ?"; 
	
	private static 	String SELECT_SFBDB_AAATR_REVERSA =
			"SELECT TVA_MOVIM as valorGasto, " +
			" GLB_DTIME as glbDtimeAAATR " +
			" FROM LINC.SFBDB_AAATR@DBLINK@" +
			" WHERE TFETRAREL = ?" + 
			" AND DCO_OFICI = ?"  +
			" AND DCO_TERMI = ?" + 
			" AND TNU_TRANS >= ?" +
			" AND DCO_TRANS = ?"  +
			" AND ACU_PRODU = ?"  + 
			" AND ACU_OFICI = ?"  +
			" AND ACUNUMCUE = ?"  +
			" AND ACUDIGVER = ?"  +
			" AND ACO_CAUSA = ?"  +
			" AND TNUDOCTRA = ?"  +
	        " AND TSE_REVER <> ?" ;

	private final String UPDATE_SFBDB_PPRGP = "UPDATE LINC.SFBDB_PPRGP@DBLINK@" + 
			"	SET PCO_ESTAD = ?," + 
			"	PSUPAGHOY = 0 ," + 
			"	PNUCOMPRO = 0" + 
			"	WHERE GLB_DTIME = ?" ;
	
	private final String SELECT_LINC_SFBDB_AAATR = "SELECT GLB_DTIME as glbDtimeAAATR," + 
			"	ACO_CAUSA as codCausal," + 
			"   ACO_CONCE as codConcepto" + 
			"	FROM LINC.SFBDB_AAATR@DBLINK@" + 
			"	WHERE TFETRAREL = ?" + 
			"	AND DCO_OFICI = ?" + 
			"	AND DCO_TERMI = ?" + 
			"	AND TNU_TRANS = ?" + 
			"	AND DCO_TRANS = ?" + 
			"	AND ACU_PRODU = ?" + 
			"	AND ACU_MONED = ?" + 
			"	AND ACU_OFICI = ?" + 
			"	AND ACUNUMCUE = ?" + 
			"	AND ACUDIGVER = ?" + 
			"	AND TNUDOCTRA = ?" + 
			"	AND TVA_MOVIM = ?" +
			"	AND TSE_REVER != ?"; 
	
	private final String UPDATE_LINC_SFBDB_AAATR = "UPDATE LINC.SFBDB_AAATR@DBLINK@" + 
			" SET TSE_REVER = ?" + 
			" WHERE GLB_DTIME = ?";
	
	
	private final String SELECT_LINC_SFBDB_PPRGP2 =
			"   SELECT SUM(PVA_GASTO) AS valorGasto " +
			"	FROM LINC.SFBDB_PPRGP@DBLINK@" + 
			"	WHERE PCU_OFICI = ? "+
			"	AND PCU_PRODU   = ?" +
			"	AND PCUNUMCUE   = ?" +
			"	AND PCO_GASTO  in (91,92,94)" +
			"	AND PFEINIAPL  >= 0" +
			"	AND PCO_ESTAD   = ?" +
			"	AND PSECOBANT   = ?" +
			"	AND PNUCOMPRO  = ?" ;

	private final String SELECT_LINC_SFBDB_PPRGP3 =
			"   SELECT PVA_GASTO AS valorGasto, " +
	        "   PCO_GASTO AS codigoGasto, "+
			"   GLB_DTIME AS glbDtime"+		
			"	FROM LINC.SFBDB_PPRGP@DBLINK@" + 
			"	WHERE PCU_OFICI = ? "+
			"	AND PCU_PRODU   = ?" +
			"	AND PCUNUMCUE   = ?" +
			"	AND PCO_GASTO  in (91,92,94) " +
			"	AND PFEINIAPL  >= 0" +
			"	AND PCO_ESTAD   = ?" +
			"	AND PSECOBANT   = ?" +
			"	AND PNUCOMPRO  = ?" ;

    
	private static String SELECT_FNC_CORREL_CANAL = "SELECT MADMIN.FNC_CORREL_CANAL(?) as numTran FROM DUAL" ;
	Logger logger = LoggerFactory.getLogger(ReversaPagoGastosAjenosPrestamoServicio.class);
	
	
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		logger.info("Iniciando servicio Reversa Pago de Gastos Ajenos");
		
		logger.debug("Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug("Cast de objeto de dominio -> ReversaGastosAjenosPeticion ");
		ReversaPagoGastosAjenosPrestamoPeticion peticion = (ReversaPagoGastosAjenosPrestamoPeticion) objetoDom;
		try {
			
			logger.debug("Iniciando validaciones iniciales de parametros...");
			validacionParametrosIniciales(peticion);
			validacionCheques((ArrayList<Cheque>) peticion.getCheques());
			
			Integer codProductoCta = Integer.parseInt(peticion.getCuentaPrestamo().substring(0, 3));
			datos.agregarDato("codProducto", codProductoCta);
			datos.agregarPropiedadesDeObjeto(peticion);
			
			logger.debug("Invocando la funcion de soporte 'Seguridad para Terminales financieros' ...");
			seguridadTerminalesFinancieros(datos);
			
			logger.debug("Creando objeto auxiliar 'CuentaPrestamo' con cuenta -> {} ", peticion.getCuentaPrestamo());
			CuentaPrestamo pcp = recuperarDatosCuentaPrestamo(peticion.getCuentaPrestamo());
			datos.agregarDato("peticion",peticion);
			logger.debug("Validacion de cuenta prestamo ...");
			validacionesCuentaPrestamo(pcp);
		
			//agregando propiedades del objeto al mapa 
			datos.agregarDato("peticion",peticion);
			datos.agregarDato("pcp", pcp);
			
			logger.debug("Validando codigo de moneda ...");
			validarMoneda(datos);
			
			logger.debug("Validando tasa IVA ...");
			validarTasaIVA(datos);
			
			
			
			logger.debug("Validando monto del gasto en AAATR");
			validarGastoAAATR(datos,peticion);
			
			logger.debug("Validando monto del gasto en PPRGP");
			validarGastoPPRGP(datos,peticion);
			

			logger.debug("Iniciando actualizacion de los valores de los gastos pagado");
			actualizarGasto(datos,pcp, peticion);		
		    
			logger.debug("Iniciando actualizacion de registro de la transaccion en el tanque de transacciones");
			actualizarTransaccion(datos);
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			Date fechaSistemaDMA = UtileriaDeDatos.fecha6ToDate(fechaSistema);
			Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistemaDMA);
			logger.debug("Invocando la funcion de 'actualización de perfiles de transacción' ...");			
			logger.debug("Generando nota del prestamo");
			datos.agregarDato("codCausal", 487);
			datos.agregarDato("codSubCausal", 0);
			
			datos.agregarDato("cuentaRelacionada", " ");			
			datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
			datos.agregarDato("codConcepto", Constantes.CONCEPTO_PP);
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("codMonedaTran", new Integer(0));
			datos.agregarDato("codEstadoRegistro", new Integer(0));
			datos.agregarDato("fechaInicialAfectaAnticipo", new Integer(0));
			datos.agregarDato("fechaFinalAfectaAnticipo", new Integer(0));
			datos.agregarDato("fechaInicialAfectaVencido", new Integer(0));
			datos.agregarDato("fechaFinalAfectaVencido", new Integer(0));
			datos.agregarDato("fechaVencimientoImpago", new Integer(0));
			datos.agregarDato("numFacturaFinalAfectada", new Integer(0));
			datos.agregarDato("numFacturaInicialAfectada", new Integer(0));
			datos.agregarDato("saldoTerceroAntesTran", BigDecimal.ZERO);
			datos.agregarDato("saldoTerceroDespuesTran", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresAnticipado", new BigDecimal(0.00));
			datos.agregarDato("tasaAnualInteresCompensatorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresMoratorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresVencido", BigDecimal.ZERO);
			datos.agregarDato("valorInteresAnticipado", BigDecimal.ZERO);
			datos.agregarDato("valorCapitalAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorMoraAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorMovimiento", BigDecimal.ZERO);
			datos.agregarDato("valorGastosAjenosFacturados", peticion.getValorMovimiento());
			datos.agregarDato("valorGastosPropiosFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorInteresCompensatorioFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorInteresVencidoAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorSeguroDanios", BigDecimal.ZERO);
			datos.agregarDato("valorOtrosSeguros", BigDecimal.ZERO);
			datos.agregarDato("valorSeguroVida",BigDecimal.ZERO);
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			datos.agregarDato("numDocumentoTran",peticion.getNumDocumentoReversa());
			datos.agregarDato("numDocumentoReversa",peticion.getNumDocumentoReversa());
			datos.agregarDato("senReversa", Constantes.SI);
			datos.agregarDato("codMonedaTran", pcp.getCodMoneda());
			datos.agregarDato("cuentaPrestamo", peticion.getCuentaPrestamo());						

			registrarNotasPrestamo(datos);
		
			//10.2 Registrar evento del prestamo
			//cuentaPrestamo ya esta
			//datos.agregarDato("fechaSistema", datos.obtenerInteger("fechaSistema"));
			//horaSistema ya esta
			datos.agregarDato("codCausal",315 ); //pedir agregar causal de pago de gastos ajenos
			datos.agregarDato("codPantalla", Constantes.ISPEC_PP215); //pedir agregar ispec_pp215
			//codCajero ya esta
			datos.agregarDato("codDestinoFondos", new Integer(0));
			datos.agregarDato("codOrigenFondos", new Integer(0));
			datos.agregarDato("codAccion", "");
			//codMoneda ya esta objeto pcp
			datos.agregarDato("numResolucion", " ");
			datos.agregarDato("numActaResolucion", " ");
			datos.agregarDato("tasaAnualInteresActicipado", new BigDecimal(0.00));
			//tasaAnualInteresCompensatorio ya esta objeto pcp
			datos.agregarDato("tasaAnualInteresMoratorio", pcp.getTasaAnualMora());
			datos.agregarDato("tasaAnualInteresVencido", pcp.getTasaAnualInteresNormal());
			datos.agregarDato("codUsuarioActual", " ");
			datos.agregarDato("codUsuarioAnterior", " ");
			datos.agregarDato("codModificacion", new Integer(0));
			datos.agregarDato("valorModificacion", " ");
			datos.agregarDato("senReversa", Constantes.SI);
			datos.agregarDato("fechaReversa", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("codAccion", " ");
			registrarEventosPrestamo(datos);			
			
			//Se invoca la función para registrar transacciones del cliente por día y globales en las tablas LINC.SFBDB_BSATR y LINC.SFBDB_BSATA
			reversarChequesPropiosRetencionesGerencia(datos);
			
			ReversaPagoGastosAjenosPrestamoRespuesta respuesta = new ReversaPagoGastosAjenosPrestamoRespuesta();
			datos.llenarObjeto(respuesta);
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO"); 
			logger.debug("Respuesta de invocacion de servicio Reversa Pago Gastos Ajenos Prestamo (PP215): " + respuesta);
			return respuesta;
			
		} catch (ServicioException se){
			logger.error("Ocurrio un error inesperado:", se.getMessage(), se);
			throw manejarMensajeExcepcionServicio(se);
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
	private void validacionParametrosIniciales(ReversaPagoGastosAjenosPrestamoPeticion peticion) throws ServicioException {
		
		if(
				!UtileriaDeDatos.isGreaterThanZero(peticion.getCodTran()) ||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getNumDocumentoReversa()) ||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getNumTran()) ||
				!UtileriaDeDatos.isNumericString(peticion.getCuentaPrestamo()) ||
				!UtileriaDeDatos.isStringOfLength(peticion.getCuentaPrestamo(), 13) ||
				!UtileriaDeDatos.isGreaterOrEquals(peticion.getValorEfectivo(), BigDecimal.ZERO) ||
				!UtileriaDeDatos.isGreaterOrEquals(peticion.getValorCheques(), BigDecimal.ZERO) ||
				!UtileriaDeDatos.isGreaterOrEquals(peticion.getValorMovimiento(), BigDecimal.ZERO) ||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getCodOficinaTran()) ||
			 	 UtileriaDeDatos.isBlank(peticion.getCodCajero()) ||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getNumCaja()))
				{
			throw new ServicioException(20010, "Parametros incompletos o no validos");
		}
	}
	
	private void validacionCheques(ArrayList<Cheque> cheques ) throws ServicioException {
		if(!UtileriaDeDatos.listIsEmptyOrNull(cheques)) {
			for (Cheque c : cheques) {
				
					Integer numCheque = c.getNumCheque();
					UtileriaDeParametros.validarParametro(c.getTipCheque(), "tipCheque del cheque " + numCheque, TipoValidacion.ENTERO_VALOR_EN, new Integer[] {1,2,3,4,5});
					
				
				switch (c.getTipCheque()) {
				case 1:
					
					UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getNumAutorizacion(), "numAutorizacion del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					break;
				case 2:
					
					UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque:" + numCheque, TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getNumAutorizacion(), "numAutorizacion del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					break;
				case 3:
					
					UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: "+ numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCodBancoCheque(), "codBancoCheque del cheque: "+ numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: "+ numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPlazaCheque(), "codPlazaCheque del cheque: "+ numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
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
					throw new ServicioException(21010, "Parametros no validos", "tipCheque del numcheque: " + numCheque );
				}
			}
		}
		
	}
	
	
	private  void validacionesCuentaPrestamo(CuentaPrestamo pcp) throws ServicioException{
		Integer estadoRecuperado = pcp.getCodEstadoPrestamo();
		Integer codigoBloqueo = pcp.getCodBloqueo();
		//Validacion del estado de la cuenta
		if (!UtileriaDeDatos.isEquals(estadoRecuperado, Constantes.PP_ESTADO_ACTIVO)&&
		   !UtileriaDeDatos.isEquals(estadoRecuperado, PP_ESTADO_VENCIDO)&&
		   !UtileriaDeDatos.isEquals(estadoRecuperado, PP_ESTADO_CASTIGADO)||
		   !UtileriaDeDatos.isEquals(codigoBloqueo,Constantes.NO ))
		    {
			logger.error("Estado de cuenta inactiva");
			throw new ServicioException(20009, "Cuenta en estado inválido para esta operación");
		}
	}
	/**
	 * M&eacutetodo obtener tasa IVA
	 * @param  datos
	 * @throws ServicioException
	 * @throws ParseException 
	 */

	private void validarTasaIVA(DatosOperacion datos) throws ServicioException, ParseException {
		
		try {
			Integer fechaSistema=datos.obtenerInteger("fechaSistema");        
			
			Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
			
			Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
			
			Object[] params = {
					Constantes.PP_COD_TASA_REFERENCIA,
					fechaSistemaAMD,
					1
			};
			
			BigDecimal tasaIva = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMVT),BigDecimal.class,params);
			
			datos.agregarDato("tasaIva", tasaIva);
			
			//datos.obtenerValor("tasaIva")
			
		} catch (TipoDatoException | ParseException  e) {
			logger.error("Error al recuperar objetos:", e.getMessage(), e);
			throw new ServicioException(20010, "Error al recuperar objeto.");
		}
	}
	
	private void validarMoneda(DatosOperacion datos) throws ServicioException {
		
		try {
			
			Object[] params = {					
					datos.obtenerInteger("codProducto")
			};
			
			Integer codMoneda = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMPR),Integer.class,params);		
			datos.agregarDato("codMoneda", codMoneda);
			

			
		} catch (TipoDatoException e) {
			logger.error("Error al recuperar objetos:", e.getMessage(), e);
			throw new ServicioException(20010, "Error al recuperar objeto codigo Moneda.");
		}
	}

	/**
	 * M&eacutetodo para validar que el monto total del movimiento coincide con la suma de los montos individuales de los valores declarados
	 * @param  datos
	 * @throws ServicioException
	
	 */
		
   private void validarGastoAAATR(DatosOperacion datos, ReversaPagoGastosAjenosPrestamoPeticion peticion)	throws ServicioException, TipoDatoException {
	   try {
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		Integer fechaRelativa = datos.obtenerInteger("fechaRelativa");
        Integer codOfiTran = datos.obtenerInteger("codOficinaTran") ;// peticion.getCodOficinaTran();
		Integer codTerminal = peticion.getCodTerminal();
		Integer numTran = peticion.getNumTran() ; 
		Integer codTran= datos.obtenerInteger("codTran");
		Integer numDocumentoTran = peticion.getNumDocumentoReversa();
		BigDecimal valorMovimiento = null;
		try {
			valorMovimiento = datos.obtenerBigDecimal("valorMovimiento");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		Object[] paramsPPRGP = {fechaRelativa,
				codOfiTran,
				codTerminal,
				peticion.getNumTran(),
				codTran,
				pcp.getCodProducto(),
				pcp.getCodOficina(),				
				pcp.getNumCuenta(),
				pcp.getDigitoVerificador(),
				Constantes.PP_CAUSAL_GASTOS_AJENOS,
				numDocumentoTran,
				Constantes.SI
				};
		

		
		BigDecimal valorGasto =new BigDecimal(0);
        
        
        Map<String, Object> queryForMap  = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAATR_REVERSA), paramsPPRGP);

		if(UtileriaDeDatos.mapIsEmptyOrNull(queryForMap)) {
			throw new ServicioException(20212, "Transacción no aparece en la base de datos");
		}

		AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
		
		Long glbDtimeAAATR = adaptador.getLong("glbDtimeAAATR");
        valorGasto =  adaptador.getBigDecimal("valorGasto");
        glbDtimeAAATR = adaptador.getLong("glbDtimeAAATR");
        datos.agregarDato("glbDtimeAAATR", glbDtimeAAATR);
        if(valorGasto.compareTo(valorMovimiento) != 0) {
  			throw new ServicioException(20018, " Valor incorrecto no coincide en la AAATR");
	    }               
	   }
	   catch (EmptyResultDataAccessException e) {
			logger.error("Error inesperado:" , e.getMessage(), e);
			throw new ServicioException(20212, "Transacción no aparece en la base de datos");
		}
		
   }
	
   private void validarGastoPPRGP(DatosOperacion datos,ReversaPagoGastosAjenosPrestamoPeticion peticion)	throws ServicioException, TipoDatoException {
	   
	   //Integer numTran = datos.obtenerInteger("numTran");
	   try {
	   CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class); 
	   Integer numTran = peticion.getNumTran() ;
	   BigDecimal valorMovimiento = peticion.getValorMovimiento();
	   Object[] paramsPPRGP = {
				pcp.getCodOficina(),
			    pcp.getCodProducto(),
				pcp.getNumCuenta(),
				3,
				2,
				numTran	};
	   
	   BigDecimal valorGasto = jdbcTemplate.queryForObject( query(SELECT_LINC_SFBDB_PPRGP2),BigDecimal.class,paramsPPRGP);
	   if (valorGasto == null) {
		   throw new ServicioException(20018, " Valor incorrecto no coincide en Gastos");
	   }
	   if (valorGasto.compareTo(valorMovimiento)!=0) {
			throw new ServicioException(20018, " Valor incorrecto no coincide en Gastos");			   
	   }
	   }
	   catch (EmptyResultDataAccessException e) {
			logger.error("Error inesperado:" , e.getMessage(), e);
			throw new ServicioException(20212, "Transacción no aparece en la base de datos");
		}
	   
   }
   
   
   private List<Map<String, Object>> recuperarDatosGastosAPagar(CuentaPrestamo pcp,ReversaPagoGastosAjenosPrestamoPeticion peticion){
		
	   Object[] paramsPPRGP = {
				pcp.getCodOficina(),
				pcp.getCodProducto(),
				pcp.getNumCuenta(),				
				Constantes.PP_ESTADO_GASTO_CANCELADO,
				Constantes.NO,
				peticion.getNumTran()
			};
			
			logger.debug( "Ejecutando sentencia SELECT_LINC_SFBDB_PPRGP3, parametros: " + Arrays.toString(paramsPPRGP));
			List<Map<String, Object>> datosGastosAPagar = jdbcTemplate.queryForList(query(SELECT_LINC_SFBDB_PPRGP3), paramsPPRGP);
			
		return datosGastosAPagar;	}
   
   /**
	 * M&eacutetodo para actualizar los valores de la cuenta corriente con los montos de efectivos
	 * @param  datos
	 * @throws ServicioException
	 */
	private void actualizarGasto (DatosOperacion datos,CuentaPrestamo pcp,ReversaPagoGastosAjenosPrestamoPeticion peticion) throws ServicioException {

		logger.debug("Recuperando datos gastos a pagar...");
		List<Map<String, Object>> datosGastosAPagar = recuperarDatosGastosAPagar(pcp,peticion);
		
	
		
		for(Map<String, Object> gasto : datosGastosAPagar) {
			AdaptadorDeMapa factura = UtileriaDeDatos.adaptarMapa(gasto);
			Long glbDtime = factura.getLong("glbDtime");
		    Object[] paramsPPRGP = {
					1,
		    		glbDtime,						
					};
			
		    ejecutarSentencia(query(UPDATE_SFBDB_PPRGP), paramsPPRGP);

		}
	
	}
	
	
	
	/**
	 * M&eacutetodo para procesar l&oacutegica reversa de pagos de cheques propios y retenciones
	 * @param  datos
	 * @throws ServicioException
	 */
	/**
	 * M&eacutetodo para validar reversión de la transacción.
	 * @param  datos
	 * @throws ServicioException
	 */
	private void validacionReversionTransaccion(DatosOperacion datos) throws ServicioException {
		
		try {
			ReversaPagoGastosAjenosPrestamoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoGastosAjenosPrestamoPeticion.class);
			Integer fechaRelativa = datos.obtenerInteger("fechaRelativa");
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			CuentaPrestamo pcc = datos.obtenerObjeto("pcc",CuentaPrestamo.class);

			Map<String, Object> queryForMap  = jdbcTemplate.queryForMap(query(SELECT_LINC_SFBDB_AAATR), fechaRelativa, peticion.getCodOficinaTran(), 
					peticion.getCodTerminal(), peticion.getNumTran(), pcc.getCodProducto(), 
					pcc.getCodMoneda(), pcc.getCodOficina(),pcc.getNumCuenta(), 
					pcc.getDigitoVerificador(), peticion.getNumDocumentoReversa(),
					peticion.getValorMovimiento(),Constantes.SI);

			if(UtileriaDeDatos.mapIsEmptyOrNull(queryForMap)) {
				throw new ServicioException(20212, "Transacción no aparece en la base de datos");
			}

			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
			
			Long glbDtimeAAATR = adaptador.getLong("glbDtimeAAATR");
			Integer codConcepto = adaptador.getInteger("codConcepto");
			Integer codCausal = adaptador.getInteger("codCausal");
			
			
			datos.agregarDato("glbDtimeAAATR", glbDtimeAAATR);
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("codConcepto", codConcepto);

		} catch (EmptyResultDataAccessException | TipoDatoException e) {
			logger.error("Error inesperado:" , e.getMessage(), e);
			throw new ServicioException(20212, "Transacción no aparece en la base de datos");
		}
	}
	
	
private void aplicarGastosPagar(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException{

	 CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
     Integer numTran = datos.obtenerInteger("numTran");
		
	 Object[] paramsPPRGP = {
				pcp.getCodOficina(),
				pcp.getCodProducto(),
				pcp.getNumCuenta(),
				3,
				2,
				numTran};
	
		logger.debug("Ejecutando sentencia SELECT_SFBDB_PPRGP3, parametros: " + paramsPPRGP.toString());     
		List<Map<String, Object>> registro= jdbcTemplate.queryForList(SELECT_LINC_SFBDB_PPRGP3, paramsPPRGP);
        AdaptadorDeMapa adaptador = null; 
   
        Long glbDtime ; 
        
        for(Map<String, Object> mapa : registro) {
            adaptador = UtileriaDeDatos.adaptarMapa(mapa);
            glbDtime = adaptador.getLong("glbDtime");
       		Object[] paramsUpadatePPRGP = {
    				1,
    				glbDtime};
               
            ejecutarSentencia(query(UPDATE_SFBDB_PPRGP), paramsUpadatePPRGP);
               
        }
		
        	

}

	
	
	
	/**
	 * M&eacutetodo encargado de actualizar transaccion en el tanque de transacciones
	 * @param  datos
	 * @throws ServicioException
	 */
	
	
	
	private void actualizarTransaccion(DatosOperacion datos) throws ServicioException {
		
		try {
			CuentaPrestamo pcc = datos.obtenerObjeto("pcc",CuentaPrestamo.class);
						
			ReversaPagoGastosAjenosPrestamoPeticion peticion;
			peticion = datos.obtenerObjeto("peticion",ReversaPagoGastosAjenosPrestamoPeticion.class);
			
			Long glbDtimeAAATR = datos.obtenerLong("glbDtimeAAATR");
			datos.agregarDato("glbDtimeAAATR", glbDtimeAAATR);
			datos.agregarDato("glbDtime", glbDtimeAAATR);
			datos.agregarDato("codTerminalTran", peticion.getCodTerminal());
			actualizarPerfilesTransaccionAAATR(datos);
			
			
			
			Object[] paramsSFBDB_AAATR  = {
					Constantes.SI,
					glbDtimeAAATR
			};
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_AAATR), paramsSFBDB_AAATR);
			
			
			
		} catch (EmptyResultDataAccessException | TipoDatoException e) {
			logger.error("Error inesperado:" , e.getMessage(), e);
			throw new ServicioException(20099, "Error");
		}
	}
	
	private void reversarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {
		ReversaPagoGastosAjenosPrestamoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoGastosAjenosPrestamoPeticion.class);
		
		//Recuperando numTran del servicio en este paso para no perder valor,
		//porque se sustituye en FS Pagos cheques, y se setea al final
		Integer numTran = datos.obtenerInteger("numTran");
		Integer numDocumentoReversa = datos.obtenerInteger("numDocumentoReversa");
		
		if (!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques())) {
			for(Cheque chk: peticion.getCheques()) {
				if(UtileriaDeDatos.isEquals(chk.getTipCheque(), 1) ||
				   UtileriaDeDatos.isEquals(chk.getTipCheque(), 2)) {
					
					datos.agregarDato("codTran", chk.getCodTran());
					datos.agregarDato("codCausal", chk.getCodCausal());
					datos.agregarDato("numTran", chk.getNumTran());
					datos.agregarDato("numReversa", chk.getNumTran());
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					datos.agregarDato("codPantalla", chk.getCodPantalla());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					
					reversaPagoCheques(datos);
				}
				else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 3)) {
					datos.agregarDato("numCheque", chk.getNumCheque());
					datos.agregarDato("valorCheque", chk.getValorCheque());
					datos.agregarDato("cuentaCheque", chk.getCuentaCheque());
					datos.agregarDato("codBancoCheque", chk.getCodBancoCheque());
					datos.agregarDato("codPlazaCheque", chk.getCodPlazaCheque());
					datos.agregarDato("numOperInternacional", chk.getNumOperInternacional());
					datos.agregarDato("cuentaDestino", peticion.getCuentaPrestamo());
					
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					
					reversaRetenciones(datos);
				}
				else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 5)) {
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

		datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
		datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
		datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
		
		datos.agregarDato("numTran", numTran);
		datos.agregarDato("numDocumentoReversa", numDocumentoReversa);
		
}

	
	
	

}
