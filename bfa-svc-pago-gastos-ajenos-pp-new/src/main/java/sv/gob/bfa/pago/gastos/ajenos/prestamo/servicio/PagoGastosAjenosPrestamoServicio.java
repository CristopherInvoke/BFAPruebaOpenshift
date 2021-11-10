package sv.gob.bfa.pago.gastos.ajenos.prestamo.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import sv.gob.bfa.core.fs.FSValorEnLetras;
import sv.gob.bfa.core.model.Cheque;

//import com.sun.xml.bind.v2.schemagen.xmlschema.List;

import sv.gob.bfa.core.model.Cliente;
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
import sv.gob.bfa.pago.gastos.ajenos.prestamo.model.PagoGastosAjenosPrestamoPeticion;
import sv.gob.bfa.pago.gastos.ajenos.prestamo.model.PagoGastosAjenosPrestamoRespuesta;



/**
 * Clase contiene logica del negocio correspondiente a Deposito mixto de cuente corriente
 */
public class PagoGastosAjenosPrestamoServicio extends Servicio{
		
	private static String SELECT_SFBDB_AAMVT =
			" SELECT ATA_VENCI/100 AS tasaIVA" + 
			" FROM LINC.SFBDB_AAMVT@DBLINK@" + 
			" WHERE ACOTASREF = ?" + 
			" AND AFETASREF <= ?" + 
			" AND ROWNUM <= ?";  

			
	private static String SELECT_LINC_SFBDB_BSMTG = "SELECT ANO_LARGA as nomTipDocumentoPersona" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE ACO_TABLA = 'DOC-IDENT'" + 
			"	AND ACO_CODIG = LPAD(?, 2, '0')";
	
	
	private static String SELECT_SFBDB_PPRGP1 = "SELECT a.PVA_GASTO AS valorGasto," + 
            " a.PSE_IVA AS senialIVA,"    +
            " B.ANO_GASTO AS nomGasto,"    +
            " A.GLB_DTIME AS glbDtime"     +
            " FROM LINC.SFBDB_PPRGP@DBLINK@ A," +
            " LINC.SFBDB_AAMGA@DBLINK@ B"       +
            " WHERE A.PCU_OFICI = ?"    +
			" AND A.PCU_PRODU =  ?"   +
			" AND A.PCUNUMCUE =  ?"   +
			" AND A.PCO_GASTO in (91,92,94)"    +
			" AND A.PFEINIAPL >= 0"   +
			" AND A.PCO_ESTAD = ?"    +
			" AND A.PSECOBANT = ?"    +
			" AND B.ACO_GASTO = A.PCO_GASTO" +
			" AND B.DCO_MONED = ?"           +
			" AND B.AMA_LIMIT >= 999999";

	private static String SELECT_SFBDB_PPRGP2 = "SELECT  PCO_ESTAD AS codigoEstado, " +
			" PVA_GASTO AS valorGasto, " +
		    " PSECOBANT AS senCobroGastos, "  +
		    " PCO_GASTO AS codigoGasto, "  +
		    " GLB_DTIME AS glbDtime"     +
		    " FROM LINC.SFBDB_PPRGP@DBLINK@" +
		    " WHERE PCU_OFICI = ?"    +
			" AND PCU_PRODU   =  ?"   +
			" AND PCUNUMCUE   =  ?"   +
			" AND PCO_GASTO   in (91,92,94)"    +
			" AND PFEINIAPL   >= ?"   +
			" AND PCO_ESTAD   = ?"    +
			" AND PSECOBANT   = ?"    ;
		
	private static String UPDATE_SFBDB_PPRGP = "UPDATE LINC.SFBDB_PPRGP@DBLINK@ " + 
			" SET PCO_ESTAD = ?," + 
			" PSUPAGHOY = ?, " + 
			" PNUCOMPRO = ? " + 
			" WHERE GLB_DTIME = ?" ; 
			
	
	
	private static String  SELECT_SFBDB_AAMPR = "SELECT DCO_MONED as codMoneda" + 
			"	FROM LINC.SFBDB_AAMPR@DBLINK@" +
			"	WHERE ACO_PRODU = ?"; 
	
	private static 	String SELECT_SFBDB_AAATR =
			"SELECT tnudoctra as cantidad" + 
			"	FROM LINC.SFBDB_AAATR@DBLINK@" +
			"	WHERE TFETRAREL = ?" + 
			"	AND DCO_OFICI = ?"  +
			"	AND DCO_TERMI = ?"  + 
			"	AND TNU_TRANS >= ?" + 
			"	AND ACU_PRODU = ?"  + 
			"	AND ACU_OFICI = ?"  +
			"	AND ACUNUMCUE = ?"  +
			"	AND ACUDIGVER = ?"  +
			"	AND ACO_CAUSA = ?"  +
			"	AND TNUDOCTRA = ?"  +
	        "	AND TSE_REVER <> ?"  ;
	
	//Query para recuperar numero de transaccion
	private static String SELECT_FNC_CORREL_CANAL = "SELECT MADMIN.FNC_CORREL_CANAL(?) as numTran FROM DUAL" ;
	
	private static final String SELECT_MADMIN_FNC_CORREL_CANAL = "SELECT MADMIN.FNC_CORREL_CANAL(?) as numTran FROM DUAL";
	public static final Integer PP_ESTADO_VENCIDO 		 			= 2;
	public static final Integer PP_ESTADO_CASTIGADO		 			= 7;
	
	Logger logger = LoggerFactory.getLogger(PagoGastosAjenosPrestamoServicio.class);
	@Transactional("transactionManager")
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		logger.info("Iniciando servicio Pago de Gastos Ajenos Prestamos");

		logger.debug("Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();
		
		logger.debug("Cast de objeto de dominio -> PagoGastosAjenosPrestamoPeticion ");
		PagoGastosAjenosPrestamoPeticion peticion = (PagoGastosAjenosPrestamoPeticion) objetoDom;
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
	        datos.agregarDato("codEstadoPrestamo", pcp.getCodEstadoPrestamo());
	        datos.agregarDato("codBloqueo", pcp.getCodBloqueo());
			logger.debug("Validacion de cuenta prestamo ...");

			logger.debug("Validacion de cuenta prestamo ...");

			validacionesCuentaPrestamo(pcp);

			
			logger.debug("Recuperacion de los datos del cliente ...");
			Cliente cliente = recuperarDatosCliente(pcp.getCodCliente());
			datos.agregarDato("cliente", cliente);
			datos.agregarPropiedadesDeObjeto(cliente);
			//agregando propiedades del objeto al mapa 
			datos.agregarDato("peticion",peticion);
		    datos.agregarDato("pcp", pcp);			

			logger.debug("Validando tasa IVA ...");
			validarTasaIVA(datos);
			
			logger.debug("Validando codigo de moneda ...");
			validarMoneda(datos);
			
			validacionMontos(datos);
			
			logger.debug("Calculando gastos");
			calcularGastosPagar(datos,peticion);			
			
			logger.debug("Aplicando gastos");
			aplicarGastosPagar(pcp,datos);
			
			logger.debug("Iniciando verificacion que no se duplique el número de documento de la transaccion");
			validacionNoReversionTransaccion(datos,pcp);			
		    
			logger.debug("Iniciando actualizacion de registro de la transaccion en el tanque de transacciones");
			
						
            Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
			
			Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
			
			logger.debug("Invocando la funcion de 'actualización de perfiles de transacción' ...");			
			logger.debug("Generando nota del prestamo");
			datos.agregarDato("codCausal",Constantes.PP_CAUSAL_GASTOS_AJENOS); //pedir agregar causal de pago de gastos ajenos			
			datos.agregarDato("codSubCausal",0);
			datos.agregarDato("cuentaRelacionada", "0000000000000");			
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
			datos.agregarDato("numDocumentoTran",peticion.getNumDocumentoTran());
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("codMonedaTran", pcp.getCodMoneda());
			datos.agregarDato("cuentaPrestamo", peticion.getCuentaPrestamo());						
            
			registrarNotasPrestamo(datos);
			
			//10.2 Registrar evento del prestamo
			
			datos.agregarDato("codPantalla", Constantes.ISPEC_PP215); //pedir agregar ispec_pp215
			datos.agregarDato("tasaAnualInteresActicipado", new BigDecimal(0.00));
			datos.agregarDato("codDestinoFondos", new Integer(0));
			datos.agregarDato("codOrigenFondos", new Integer(0));
			datos.agregarDato("numResolucion", " ");
			datos.agregarDato("numActaResolucion", " ");
			datos.agregarDato("codUsuarioActual", " ");
			datos.agregarDato("codUsuarioAnterior", " ");
			datos.agregarDato("codModificacion", new Integer(0));
			datos.agregarDato("valorModificacion", " ");
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("fechaReversa", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("codCausal", Constantes.PP_CAUSAL_GASTOS_AJENOS);
			datos.agregarDato("codAccion", " ");
			registrarEventosPrestamo(datos);		
			
			logger.debug("Iniciando registro de la transaccion en el tanque de transacciones");
			datos.agregarDato("codCausal",487); //pedir agregar causal de pago de gastos ajenos
			registroTransaccion(datos);
			validarTasaIVA(datos);
			
			logger.debug(" Iniciando proceso para lógica de pago de cheques propios y retenciones");
			procesarChequesPropiosRetencionesGerencia(datos);
			
			String valorTotalCobroEnLetrasRecibo = "";
			FSValorEnLetras fsValorLetras = new FSValorEnLetras();
			valorTotalCobroEnLetrasRecibo = fsValorLetras.convertir(peticion.getValorMovimiento().toString());
			datos.agregarDato("valorTotalCobroEnLetrasRecibo", valorTotalCobroEnLetrasRecibo);
			
			PagoGastosAjenosPrestamoRespuesta respuesta = new PagoGastosAjenosPrestamoRespuesta();
			datos.llenarObjeto(respuesta);
			datosRespuesta(datos, respuesta);
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			logger.debug("Respuesta de invocacion PAGO DE GASTOS AJENOS (PP215): " + respuesta);
			return respuesta;
			
		} catch (ServicioException se) {
			logger.error("Ocurrio un error inesperado:", se.getMessage(), se);
			throw manejarMensajeExcepcionServicio(se);
		}
		catch (TipoDatoException | ParseException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw new ServicioException(20001, "Error inesperado: " + e.getMessage());			
		}
		
	}

	private void datosRespuesta(DatosOperacion datos, PagoGastosAjenosPrestamoRespuesta respuesta) throws TipoDatoException {
		
		PagoGastosAjenosPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoGastosAjenosPrestamoPeticion.class);
		respuesta.setValorMovimiento(peticion.getValorMovimiento());
		respuesta.setValorEfectivo(peticion.getValorEfectivo());
		respuesta.setValorCheques(peticion.getValorCheques());
		respuesta.setValorChequesAjenos(peticion.getValorChequesAjenos());
		respuesta.setValorChequesPropios(peticion.getValorChequesPropios());
		
	}
	/**
	 * M&eacutetodo para validar parametros iniciales 
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionParametrosIniciales(PagoGastosAjenosPrestamoPeticion peticion) throws ServicioException {
		
		if(
				!UtileriaDeDatos.isGreaterThanZero(peticion.getCodTran()) ||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getNumDocumentoTran()) ||
//				!UtileriaDeDatos.isNumericString(peticion.getCuentaCorriente()) ||
//				!UtileriaDeDatos.isStringOfLength(peticion.getCuentaCorriente(), 13) ||
//              !UtileriaDeDatos.isGreaterThanZero(peticion.getTipDocumentoPersona()) ||
//				UtileriaDeDatos.isBlank(peticion.getNumDocumentoPersona()) ||
//				UtileriaDeDatos.isBlank(peticion.getNombrePersona()) ||				
				!UtileriaDeDatos.isGreaterOrEquals(peticion.getValorEfectivo(), BigDecimal.ZERO) ||
				!UtileriaDeDatos.isGreaterOrEquals(peticion.getValorCheques(), BigDecimal.ZERO) ||
				!UtileriaDeDatos.isGreaterOrEquals(peticion.getValorMovimiento(), BigDecimal.ZERO) ||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getCodOficinaTran()) ||
//				 UtileriaDeDatos.isBlank(peticion.getCodCajero()) ||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getNumCaja())||
				!UtileriaDeDatos.isGreaterThanZero(peticion.getSenSupervisor())
				){
			throw new ServicioException(21010, "Parámetros no válidos");
		}
	}
	private void validacionMontos(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		PagoGastosAjenosPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoGastosAjenosPrestamoPeticion.class);
		
		BigDecimal montoTotal = peticion.getValorEfectivo()
								.add(peticion.getValorChequesPropios())
								.add(peticion.getValorChequesAjenos());
		
		if (!UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), montoTotal)) {
			throw new ServicioException(20286, "Valor del movimiento no está cuadrado");
		}
		
		BigDecimal montoCheques = peticion.getValorChequesPropios()
									.add(peticion.getValorChequesAjenos());
		
		if (!UtileriaDeDatos.isEquals(montoCheques, peticion.getValorCheques())) {
			throw new ServicioException(20286, "Valor del movimiento no está cuadrado");
		}
		
		if (
			(UtileriaDeDatos.isGreater(peticion.getValorChequesPropios(), BigDecimal.ZERO) ||
			 UtileriaDeDatos.isGreater(peticion.getValorChequesAjenos(), BigDecimal.ZERO))
							&&
			 (UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques()))
			) {
			throw new ServicioException(21286, "No se han recibido los datos de los cheques correspondientes al monto");
		}
				
		BigDecimal sumMontoChequesPropios = BigDecimal.ZERO;
		BigDecimal sumMontoChequesAjenos = BigDecimal.ZERO;
		
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

	
	
	
//	
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
//	
//	
//	
//	
	
	/**
	 * M&eacutetodo obtener tasa IVA
	 * @param  datos
	 * @throws ServicioException
	 */

	private void validarTasaIVA(DatosOperacion datos) throws ServicioException {
		
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
			throw new ServicioException(21011, "Error al recuperar objeto.");
		}
	}
	
	/**
	 * M&eacutetodo Calcular gastos a pagar
	 * @param  datos
	 * @throws ServicioException
	 * @throws ParseException 
	 * @throws TipoDatoException 
	 */

	private void calcularGastosPagar(DatosOperacion datos,PagoGastosAjenosPrestamoPeticion peticion) throws ServicioException, TipoDatoException, ParseException{
		    
		
			
			List<Map<String, Object>> registro= recuperaDatosGasto(datos);
            
            BigDecimal valorGasto =new BigDecimal(0);
            BigDecimal valorZero = new BigDecimal(0);
            BigDecimal valorFactura = new BigDecimal(0);
            BigDecimal valorMovimiento = new BigDecimal(0);
            Integer senialIVA = 0;
            BigDecimal iva = new BigDecimal(0);
            BigDecimal tasaIva;
		    tasaIva = datos.obtenerBigDecimal("tasaIva");
            AdaptadorDeMapa adaptador = null; 
               
            for(Map<String, Object> mapa : registro) {
            	   adaptador = UtileriaDeDatos.adaptarMapa(mapa);    
                   valorGasto =  adaptador.getBigDecimal("valorGasto");
                   senialIVA = adaptador.getInteger("senialIVA");
                   
                   if(valorGasto.doubleValue() != 0) {
                	   valorFactura = valorGasto;
                   }
                   
                                                     		   
                   if (senialIVA == Constantes.SI)
                   { iva = valorFactura.multiply(tasaIva); 
                	   
                   }
                	valorMovimiento = valorMovimiento.add(valorFactura);	   
                	logger.info(" valor movimiento "+valorMovimiento);
            }
			
			if (valorMovimiento.compareTo(valorZero)== 0) {
				throw new ServicioException(21018, "Debe pagar total de gastos");	
			}
			
			if (peticion.getValorMovimiento().compareTo(valorMovimiento)!=0) {
				throw new ServicioException(20018, " Valor incorrecto no coincide en Gastos");			   
		   }
            	

	}
	
	private List<Map<String, Object>> recuperaDatosGasto(DatosOperacion datos) throws TipoDatoException{
		
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		Object[] paramsGasto = {
				pcp.getCodOficina(),
				pcp.getCodProducto(),
				pcp.getNumCuenta(),				
				new Integer(0),
				new Integer(1),
				new Integer(2)
				};
		System.out.println("paramsGasto"+paramsGasto);
		logger.debug("Ejecutando sentencia SELECT LINC SFBDB PPMFA 1, parametros: {}", Arrays.toString(paramsGasto));
		List<Map<String, Object>> queryForList = this.jdbcTemplate.queryForList(query(SELECT_SFBDB_PPRGP2), paramsGasto);
		
		return queryForList;
	}

	private void aplicarGastosPagar(CuentaPrestamo pcp,DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException{
		
		Object[] paramsGasto = {
				pcp.getCodOficina(),
				pcp.getCodProducto(),
				pcp.getNumCuenta(),
				1,
				2,
				2};
		logger.debug("Ejecutando sentencia SELECT_SFBDB_PPRGP1, parametros: " + paramsGasto.toString());     
		List<Map<String, Object>> registro= jdbcTemplate.queryForList(query(SELECT_SFBDB_PPRGP1), paramsGasto);
        BigDecimal valorGasto =new BigDecimal(0);
        AdaptadorDeMapa adaptador = null; 
        Integer numTran	= jdbcTemplate.queryForObject(SELECT_MADMIN_FNC_CORREL_CANAL, Integer.class, Constantes.VENTANILLA);
        datos.agregarDato("numTran", numTran);
        Long glbDtime ; 
        Integer contador = 0;
        String pnoGasto1;
        String pnoGasto2;
        String pnoGasto3;
        String pnoGasto4;
        String pnoGasto5;
        for(Map<String, Object> mapa : registro) {
        	contador ++;
            adaptador = UtileriaDeDatos.adaptarMapa(mapa);
            valorGasto =  adaptador.getBigDecimal("valorGasto");
            glbDtime = adaptador.getLong("glbDtime");
            
            System.out.println("glbDtime "+ glbDtime);
            
       		Object[] paramsUpadatePPRGP = {
    				3,
    				valorGasto,
    				numTran,
    				glbDtime};
               
            ejecutarSentencia(query(UPDATE_SFBDB_PPRGP), paramsUpadatePPRGP);
            switch (contador) {
            case 1:
               pnoGasto1= adaptador.getString("nomGasto");
               datos.agregarDato("pnoGasto1", pnoGasto1);
               break;
            case 2:
                pnoGasto2= adaptador.getString("nomGasto");
                datos.agregarDato("pnoGasto2", pnoGasto2);
                break;
            case 3:
                pnoGasto3= adaptador.getString("nomGasto");
                datos.agregarDato("pnoGasto3", pnoGasto3);
                break;
            case 4:
                pnoGasto4= adaptador.getString("nomGasto");
                datos.agregarDato("pnoGasto4", pnoGasto4);
                break;
            case 5:
                pnoGasto5= adaptador.getString("nomGasto");
                datos.agregarDato("pnoGasto5", pnoGasto5);
                break;
            }
            
        }
		
        	

}


	
	
	/**
	 * M&eacutetodo para validar que el monto total del movimiento coincide con la suma de los montos individuales de los valores declarados
	 * @param  datos
	 * @throws ServicioException
	 */

	private void validarMoneda(DatosOperacion datos) throws ServicioException {
		
		try {
			
			Object[] params = {					
					datos.obtenerInteger("codProducto")
			};
			Integer codMoneda = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMPR),Integer.class,params);
			
			datos.agregarDato("codMoneda", codMoneda);
			

			
		} catch (TipoDatoException e) {
			logger.error("Error al recuperar objetos:", e.getMessage(), e);
			throw new ServicioException(21012, "Error al recuperar objeto codigo Moneda.");
		}
	}
	
	
	/**
	 * M&eacutetodo para validar que no se duplique el número de documento de la transacci&oacuten
	 * @param  datos
	 * @throws ServicioException
	 */
	private void validacionNoReversionTransaccion (DatosOperacion datos,CuentaPrestamo pcp) throws ServicioException {
		
		Integer resultado = null;
		
		try {
			
			//CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			PagoGastosAjenosPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoGastosAjenosPrestamoPeticion.class);
			
			Object[] paramsAAATR = {
					datos.obtenerInteger("fechaRelativa"),
					datos.obtenerInteger("codOficinaTran"),
					datos.obtenerInteger("codTerminal"),
					0,
					pcp.getCodProducto(),
					pcp.getCodOficina(),					
					pcp.getNumCuenta(),
					pcp.getDigitoVerificador(),
					Constantes.PP_CAUSAL_GASTOS_AJENOS,
					peticion.getNumDocumentoTran(),
				    Constantes.SI
			};
			try {
			resultado = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAATR),Integer.class,paramsAAATR);
			
			}catch (EmptyResultDataAccessException e) {
				
			}
			
			if(!UtileriaDeDatos.isNull(resultado)) {
				throw new ServicioException(20004, "Documento ya existe registrado en la base de datos");
			} 
		} catch (TipoDatoException e) {
			logger.error("Error recuperando objetos:", e.getMessage(), e);
			throw new ServicioException(20009, "Error recuperando objetos auxiliares.");
		}
		
	}
	

	private void procesarChequesPropiosRetencionesGerencia(DatosOperacion datos) throws ServicioException, TipoDatoException {
		
		PagoGastosAjenosPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoGastosAjenosPrestamoPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
		//Recuperando numTran del servicio en este paso para no perder valor,
		//porque se sustituye en FS Pagos cheques, y se setea al final
		Integer numTran = datos.obtenerInteger("numTran");
		Integer numDocumentoTran = datos.obtenerInteger("numDocumentoTran");
		
		if (!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques())) {
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
					datos.agregarDato("codPantalla", chk.getCodPantalla());
					
					datos.agregarDato("tipDocumentoPersona", peticion.getTipDocumentoPersona());
					datos.agregarDato("numDocumentoPersona", peticion.getNumDocumentoPersona());
					datos.agregarDato("nombrePersona", peticion.getNombrePersona());
					datos.agregarDato("codCajero", peticion.getCodCajero());
					datos.agregarDato("numCaja", peticion.getNumCaja());
					datos.agregarDato("codTerminal", peticion.getCodTerminal());
					datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
					datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
					datos.agregarDato("nomOficina",datos.obtenerString("nomOficinaTran"));			
					pagosCheques(datos);
					chk.setNumTran(datos.obtenerInteger("numTran"));
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
					
					retenciones(datos);
				}
				else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 5)) {					
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
		datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
		datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
		datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
		
		datos.agregarDato("numTran", numTran);
		datos.agregarDato("numDocumentoTran", numDocumentoTran);
		datos.agregarDato("codCliente", cliente.getCodCliente());
}

	/**
	 * M&eacutetodo para registrar la transacci&oacuten en el tanque de transacciones
	 * @param  datos
	 * @throws ServicioException
	 */
	private void registroTransaccion (DatosOperacion datos) throws ServicioException {
		
		try {
			PagoGastosAjenosPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoGastosAjenosPrestamoPeticion.class);
			CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			
			

			String descripcionTran =" ";

			Integer numTran			= datos.obtenerInteger("numTran");
			
			datos.agregarPropiedadesDeObjeto(peticion);
			datos.agregarPropiedadesDeObjeto(pcp);
			datos.agregarDato("codOficina", pcp.getCodOficina());
			datos.agregarDato("codProducto", pcp.getCodProducto());
			datos.agregarDato("numCuenta", pcp.getNumCuenta());
			datos.agregarDato("digitoVerificador", pcp.getDigitoVerificador());			
			datos.agregarDato("codConcepto", Constantes.CONCEPTO_PP);
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			//fechaRelativa ya esta por la seguridad
			datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("codCompania", pcp.getCodCompania());
			
			datos.agregarDato("descripcionTran", descripcionTran);
			datos.agregarDato("codDebCre", Constantes.CREDITO);
			datos.agregarDato("numDocumentoReversa", new Integer(0));
			datos.agregarDato("senAJATR", Constantes.NO);
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("senWANG", new Integer(0));
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.NO);
			datos.agregarDato("senPosteo", Constantes.NO);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", new BigDecimal(1));
			datos.agregarDato("valorVenta", new BigDecimal(1));
			datos.agregarDato("numDocumentoTran2", new Integer(0));
			datos.agregarDato("valorChequesAjenos", BigDecimal.ZERO);
			datos.agregarDato("valorChequesExt", BigDecimal.ZERO);
			datos.agregarDato("valorChequesPropios", BigDecimal.ZERO);			
			datos.agregarDato("numCuentaTransf", "0000000000000");
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("codCausal", 487);
			datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
			datos.agregarDato("numCaja", peticion.getNumCaja());
			//montoIva -> null
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("codCajero", peticion.getCodCajero());
			registrarTransaccionAAATR(datos);

		} catch (TipoDatoException e) {
			logger.error("Error inesperado:" + e.getMessage(), e);
			throw new ServicioException(20099, "Error");
		}
	}
	

}
