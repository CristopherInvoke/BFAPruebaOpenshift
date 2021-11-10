package sv.gob.bfa.pagoprestamo.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;

import sv.gob.bfa.core.model.Cheque;
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
import sv.gob.bfa.pagoprestamo.model.ReversaPagoPrestamoPeticion;
import sv.gob.bfa.pagoprestamo.model.ReversaPagoPrestamoRespuesta;

public class PagoPrestamoReversaServicio extends Servicio{
	
	private static final String NOM_COD_SERVICIO = "Reversa Pago prestamo PP210: ";
	private static final String SELECT_SFBDB_PPMFA_FACT_PAGADA2= 
			"select pfe_estad as fechaEstadoFactura," + 
	        "   PCU_OFICI as codigoOficina,"+
			"   PCU_PRODU as codigoProducto,"+
	        "   PCUNUMCUE as numeroCuenta,"+				        
			"	psepagmor as senPagoInteresMoratorio," + 
			"	psepagven as senPagoInteresCompensatorio," + 
			"	psepagext as senPagoGastoAjeno," + 
			"	psepagpro as senPagoGastoPropio," + 
			"	psepagint as senPagoInteresNormal," + 
			"	pco_estad as codEstadoFactura," + 
			"	pfepagmor as fechaPagoMora," + 
			"	pfepagven as fechaPagoInteresCompensatorio," + 
			"	pfepagext as fechaPagoGastoAjeno," + 
			"	pfepagpro as fechaPagoGastoPropio," + 
			"	pfepagint as fechaPagoInteres," + 
			"	pfe_estad as fechaEstado," + 
			"	pvapagmor as valorInteresMoratorioPagado," + 
			"	pvapagven as valorInteresCompenPagado, " + 
			"	pvapagext as valorGastoAjenoPagado," + 
			"	pvapagpro as valorGastoPropioPagado," + 
			"	pvapagint as valorInteresNormalPagado," + 
			"	pvapagcap as valorCapitalPagado," + 
			"	pvaintext as valorInteresCompenFacturado," + 
			"	pva_mora as valorInteresMoraFacturado," + 
			"	pva_capit as valorCapital," + 
			"	pfe_venci as fechaVencimientoFactura," + 
			"	PNU_FACTU AS numFactura," + 
			"	GLB_DTIME AS glbdtimeppmfa" + 
			"	from linc.sfbdb_ppmfa@DBLINK@" + 
			"	where pcu_ofici = ?" + 
			"	and pcu_produ = ?" + 
			"	and pcunumcue = ?" + 
			"	and (pco_estad = ?" + 
			"	or pco_estad = ?)" +			 
			"   and rownum= 1 "+
			"	ORDER BY GLB_DTIME DESC"
			;
	
	
	private static String SELECT_SUM_SFBDB_AAATR= "select nvl(sum(tva_movim),0) AS valorTotal" + 
			"	from linc.sfbdb_aaatr@DBLINK@" + 
			"	where tfetrarel = ?" + 
			"	and dco_ofici = ?" + 
			"	and dco_trans = ?" + 
			"	and dco_termi = ?" + 
			"	and tnu_trans = ?" + 
			"	and acu_produ = ?" + 
			"	and acu_ofici = ?" + 
			"	and acunumcue = ?" + 
			"	and acudigver = ?" + 
			"	and tnudoctra = ?" + 
			"	and (aco_causa = ? or aco_causa = ?)" + 
			"	and TSE_REVER != ?";
	
	private static String SELECT_GLB_DTIME ="select glb_dtime AS glbDtimeAAATR" + 
			"	from linc.sfbdb_aaatr@DBLINK@" + 
			"	where tfetrarel = ?" + 
			"	and dco_ofici = ?" + 
			"	and dco_trans = ?" + 
			"	and dco_termi = ?" + 
			"	and tnu_trans = ?" + 
			"	and acu_produ = ?" + 
			"	and acu_ofici = ?" + 
			"	and acunumcue = ?" + 
			"	and acudigver = ?" + 
			"	and tnudoctra = ?" + 
			"	and TSE_REVER != ?";

	private static String SELECT_MAX_NUM_TRAN = 
			"SELECT nvl(MAX(TNU_TRANS),0) NUMTRANMAYOR FROM LINC.SFBDB_AAATR@DBLINK@ "+
			"WHERE  TFETRAREL = ? "+
			"AND  ACU_PRODU = ? "+
			"AND ACU_OFICI = ? "+
			"AND ACUNUMCUE = ? "+
			"AND DCO_TRANS = 43 "+
			"AND ACO_CAUSA IN (44,45) "+
			"AND TSE_REVER != 1";
	
	private static String UPDATE_SFBDB_AAATR = "UPDATE linc.sfbdb_aaatr@DBLINK@ SET TSE_REVER = ? WHERE glb_dtime = ?"; 
	
	private static String UPDATE_SFBDB_PPMPR = "UPDATE LINC.SFBDB_PPMPR@DBLINK@" + 
			"	SET PSAULTFAC = ?," + 
			"	PSEPRIPER = ?," + 
			"	PPEPAGINT = ?," + 
			"	PDIPRXVEN = ?," + 
			"	PFEVENCAP = ?," + 
			"	PFE_VENCI = ?," + 
			"	PFEPRXFAC = ?," + 
			"	PTA_INTER = ?," + 
			"	PTAINTEXT = ?," + 
			"	PTA_MORA = ?," + 
			"	PCNCUOADE = ?" + 
			"	WHERE GLB_DTIME = ?";
	
	private static String SELECT_DEST_PRESTAMO = "SELECT ANO_CORTA AS destinoPrestamo" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE ACO_TABLA = ?" + 
			"	AND ACO_CODIG = ?";
	
	private static String SELECT_NOM_PRESTAMO = "SELECT ANO_CORTA AS nombreEstadoPrestamo" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE ACO_TABLA = ?" + 
			"	AND ACO_CODIG = ?";
	private static String SELECT_ULTIMA_FACTURA = "select max(pnu_Factu)+1 numero_factura from linc.sfbdb_ppmfa@DBLINK@ " + 
			" where pcu_produ = ? " + 
			"and pcu_ofici = ? " + 
			"and pcunumcue =? " ; 
		
	private static String UPDATE_SFBDB_PPMPR2 = "UPDATE LINC.SFBDB_PPMPR@DBLINK@ SET PSUINTFAC = ?," + 
			"   PSUCAPPHO = ?," + 
			"   PSUCAPPME = ?," + 
			"   PSUCAPPAN = ?," + 
			"   PSUCAPRME = ?," + 
			"   PSUCAPRAN = ?," + 
			"   PSUMORPHO = ?," + 
			"   PSUMORPCR = ?," + 
			"   PSUMORRME = ?," + 
			"   PSUMORDEV = ?," + 
			"   PSUMORAJD = ?," + 
			"   PSUMORCON = ?," + 
			"   PSUMORCCR = ?," + 
			"   PSUEXTPHO = ?," + 
			"   PSUEXTPCR = ?," + 
			"   PSUEXTRME = ?," + 
			"   PSUEXTRAN = ?," + 
			"   PSUEXTDEV = ?," + 
			"   PSUEXTAJD = ?," + 
			"   PSUEXTCON = ?," + 
			"   PSUEXTCCR = ?," + 
			"   PFEACUDEV = ?," + 
			"   PFEULTPAG = ?," + 
			"   PNU_FACTU = ?," + 
			"   PSA_TEORI = ?," + 
			"   PSA_REAL  = ?," + 
			"   PSA_VENCI = ?," + 
			"   PSATERHOY = ?," + 
			"   PSATERDSI = ?," + 
			"   PCN_PAGOS = ?," + 
			"   PCNCUOPAG = ?," + 
			"   PNUFACIMP = ?," + 
			"   PFEVENIMP = ?," + 
			"   PDIDESVEN = ?," + 
			"   PFEPRXPRO = ?," + 
			"   PSUMORPRO = ?," + 
			"   PSATOTMES = ?," + 
			"   PFEPRXEVE = ?," + 
			"   PFEVENIMPI = ?," + 
			"   PFEVENIMPK = ?," + 
			"	PSAINTVEN = ?," + 
			"   PSAMORVEN = ?," + 
			"   PSACOMVEN = ?," + 
			"	PSUINTPHO = ?," + 
			"	PSUINTPCR = ?," + 
			"	PCN_TRANS = PCN_TRANS + ?" + 
			"	WHERE GLB_DTIME = ?";
	
	private static String SELECT_PPANB_VALOR_TERCERO =
	" SELECT NVL(PSATERANT,0) valorTercero " +
	" FROM LINC.SFBDB_PPANB@DBLINK@" +
	" WHERE PCU_PRODU = ?" +
	" AND PCU_OFICI = ?" +
	" AND PCUNUMCUE = ?" +
	" AND DCO_ISPEC = ?" +
	" AND PFE_EVENT = ?" +
	" AND TNUDOCTRA = ?";

	
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		
		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");

		try {
		
			logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion ...");
			DatosOperacion datos = crearDatosOperacion();
			
			logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> ReversaPagoPrestamoPeticion ");
			ReversaPagoPrestamoPeticion  peticion = (ReversaPagoPrestamoPeticion) objetoDom;
			
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			validacionParametrosIniciales(peticion);
			validacionCheques((ArrayList<Cheque>) peticion.getCheques());
			
			Integer codProductoCta = Integer.parseInt(peticion.getCuentaPrestamo().substring(0, 3));
			datos.agregarDato("codProducto", codProductoCta);
			
			logger.debug(NOM_COD_SERVICIO + "Invocando la funcion de soporte 'Seguridad para Terminales financieros' ...");
			//Agregando parametros de entrada para funcion de soporte.
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTran", peticion.getCodTran());
			seguridadTerminalesFinancieros(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Creando objeto auxiliar 'CuentaPrestamo' con cuenta -> {} ", peticion.getCuentaPrestamo());
			CuentaPrestamo pcp = recuperarDatosCuentaPrestamo(peticion.getCuentaPrestamo());
			
			//agregando propiedades del objeto al mapa 
			datos.agregarDato("peticion",peticion);
			datos.agregarDato("pcp", pcp);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperacion de los datos del cliente ...");
			Cliente cliente = recuperarDatosCliente(pcp.getCodCliente());
			datos.agregarPropiedadesDeObjeto(cliente);
			
			logger.debug(NOM_COD_SERVICIO + "validaciones de la transacción de pago de préstamo");
			validacionTransaccionPagoPrestamo(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Realizando la reversa del pago de préstamo");
			reversaPagoPrestamo(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Registrar evento y notas préstamo");
			registrarEventoYNotaPrestamo(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Se invoca la función de actualización de perfiles de transacción.");
			actualizarPerfilTransaccion(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Iterando colección de cheques enviando hacia la función de reversa de pago de cheques o hacia la reversa de retenciones");
			procesarChequesPropiosYRetenciones(datos);
			

			logger.debug(NOM_COD_SERVICIO + "Preparando objeto de respuesta ...");
			
			ReversaPagoPrestamoRespuesta respuesta = new ReversaPagoPrestamoRespuesta();
			datos.llenarObjeto(respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			if(logger.isDebugEnabled()) {
				logger.debug(NOM_COD_SERVICIO + "RESPUESTA: {} ", respuesta);
			}
			
			return respuesta;
		
		} catch (ServicioException se) {
			logger.error("Ocurrio un error inesperado:", se.getMessage(), se);
			throw manejarMensajeExcepcionServicio(se);
		}catch (TipoDatoException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado " + e.getMessage()));
		}
		
	}
	
	private void actualizarPerfilTransaccion(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		ReversaPagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoPrestamoPeticion.class);

		Object glbDtime = datos.obtenerValor("glbDtime");
		Object numTran = datos.obtenerValor("numTran");
		List<Long> glbDtimeAAATRList = (List<Long>) datos.obtenerValor("glbDtimeAAATRList");
		for (Long glbDtimeAAATR : glbDtimeAAATRList) {
			datos.agregarDato("glbDtime", glbDtimeAAATR);
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminalTran", peticion.getCodTerminal());
			//Fecha relativa puesta por seguridad
			datos.agregarDato("numTran", peticion.getNumReversa());//TODO Preguntar sobre este numero de reversa.
			actualizarPerfilesTransaccionAAATR(datos);
		}
		
		datos.agregarDato("glbDtime", glbDtime);
		datos.agregarDato("numTran", numTran);
		
	}

	/**
	 * M&eacutetodo para validar parametros iniciales 
	 * @param datos
	 * @throws ServicioException
	 */
	private void validacionParametrosIniciales(ReversaPagoPrestamoPeticion peticion) throws ServicioException {
		
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		if (!UtileriaDeDatos.isGreaterOrEquals(peticion.getNumReversa(),new Integer(0)))
				{
			logger.error(NOM_COD_SERVICIO + "Parametros incorrectos. Parametro valorCheques debe ser mayor a cero.");
			throw new ServicioException(21010, "Parametro {} debe ser mayor a cero.", "numReversa");
		} 
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCuentaPrestamo(), "cuentaPrestamo", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaPrestamo(), "cuentaPrestamo", TipoValidacion.CADENA_NUMERICA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaPrestamo(), "cuentaPrestamo", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
		UtileriaDeParametros.validarParametro(peticion.getTipDocumentoPersona(), "tipDocumentoPersona", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoPersona(), "numDocumentoPersona", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNombrePersona(), "nombrePersona", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumTransLavado(), "numTransLavador", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorCheques(), "valorCheques", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);
		
		if (
				//Si valor de valorEfectivo = 0.00 entonces valorCheques > 0.00
				( UtileriaDeDatos.isEquals(peticion.getValorEfectivo(), BigDecimal.ZERO) 
											&& 
				  !UtileriaDeDatos.isGreater(peticion.getValorCheques(), BigDecimal.ZERO)
				)
			) {
			logger.error(NOM_COD_SERVICIO + "Parametros incorrectos. Parametro valorCheques debe ser mayor a cero.");
			throw new ServicioException(21010, "Parametro {} debe ser mayor a cero.", "valorCheques");
		}
		
		if (
				//Si valor de valorCheques = 0.00 entonces valorEfectivo > 0.00
				( UtileriaDeDatos.isEquals(peticion.getValorCheques(), BigDecimal.ZERO)
											&&
				  !UtileriaDeDatos.isGreater(peticion.getValorEfectivo(), BigDecimal.ZERO)
				)
			) {
			logger.error(NOM_COD_SERVICIO + "Parametros incorrectos. Parametro valorEfectivo debe ser mayor a cero.");
			throw new ServicioException(21010, "Parametro {} debe ser mayor a cero.", "valorEfectivo");
		}
		
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
				
				switch (c.getTipCheque()) {
				case 1:
					
					UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: ", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: ", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla del cheque: ", TipoValidacion.CADENA_VACIA);
					break;
				case 2:
					
					UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodTran(), "codTran del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodCausal(), "codCausal del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCodPantalla(), "codPantalla del cheque: ", TipoValidacion.CADENA_VACIA);
					break;
				case 3:
					
					UtileriaDeParametros.validarParametro(c.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(c.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(c.getCodBancoCheque(), "codBancoCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(c.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					
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

	
	private void validacionTransaccionPagoPrestamo(DatosOperacion datos) throws ServicioException {
		
		ReversaPagoPrestamoPeticion peticion;
		CuentaPrestamo pcp;
		try {
			peticion = datos.obtenerObjeto("peticion",ReversaPagoPrestamoPeticion.class);
			pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			
			Date fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
			Integer fechaPago = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
			datos.agregarDato("numTran", peticion.getNumReversa());
			datos.agregarDato("sumInteresDevengadoNoFacturadoV", pcp.getSumInteresDevengadoNoFacturado());
			datos.agregarDato("codEstadoPrestamo", pcp.getCodEstadoPrestamo());
			datos.agregarDato("codBloqueo", pcp.getCodBloqueo());
			logger.debug("Validacion de estado prestamo");
			validarEstadoPrestamos(datos);
			
			logger.debug("Validacion de reversion");
			
			Object[] paramsSumAAATR = {
					datos.obtenerInteger("fechaRelativa"),
					peticion.getCodOficinaTran(),
					peticion.getCodTran(),
					peticion.getCodTerminal(),
					peticion.getNumReversa(),
					pcp.getCodProducto(),
					pcp.getCodOficina(),
					pcp.getNumCuenta(),
					pcp.getDigitoVerificador(),
					peticion.getNumDocumentoTran(),
					Constantes.PP_CAUSAL_EFECTIVO,
					Constantes.PP_CAUSAL_CHEQUES,
					Constantes.SI
				};
			
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT SUM SFBDB AAATR, parametros: {}", Arrays.toString(paramsSumAAATR));
			BigDecimal valorTotal = jdbcTemplate.queryForObject(query(SELECT_SUM_SFBDB_AAATR), BigDecimal.class, paramsSumAAATR);
			logger.debug(NOM_COD_SERVICIO + "valorTotal recuperado: {}", valorTotal);
			if(!UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), valorTotal)) {
				throw new ServicioException(20212, "Transacción no aparece en la base de datos");
			}
			try {
			logger.debug("Verificacion de la existencia de un resgistro posterior en la AAATR");
			
			Object[] paramsAAATRMax = {
					datos.obtenerInteger("fechaRelativa"),
					pcp.getCodProducto(),
					pcp.getCodOficina(),
					pcp.getNumCuenta()			
				};
		
			Integer numTranMax = jdbcTemplate.queryForObject(query(SELECT_MAX_NUM_TRAN ), Integer.class, paramsAAATRMax);
			
			
		
				if (!UtileriaDeDatos.isGreaterOrEquals(peticion.getNumReversa(),numTranMax)) {
					throw new ServicioException(21212, "EXISTE TRANSACCION POSTERIOR");
							
					
				}
			} catch (EmptyResultDataAccessException erdae) {
				logger.error(NOM_COD_SERVICIO + "Existe un Transacción Posterior");
				throw new ServicioException(20212, "Transacción no aparece en la base de datos");
			}
			logger.debug("Verificacion existencia de registro en AAATR");
			
			Object[] paramsAAATR = {
					datos.obtenerInteger("fechaRelativa"),
					peticion.getCodOficinaTran(),
					peticion.getCodTran(),
					peticion.getCodTerminal(),
					peticion.getNumReversa(),
					pcp.getCodProducto(),
					pcp.getCodOficina(),
					pcp.getNumCuenta(),
					pcp.getDigitoVerificador(),
					peticion.getNumDocumentoTran(),
					Constantes.SI
				};
			try {	
				List<Long> glbDtimeAAATR = jdbcTemplate.queryForList(query(SELECT_GLB_DTIME), Long.class, paramsAAATR);
				datos.agregarDato("glbDtimeAAATRList", glbDtimeAAATR);
				datos.agregarDato("fechaPago", fechaPago);
			
			} catch (EmptyResultDataAccessException erdae) {
				logger.error(NOM_COD_SERVICIO + "Transacción no aparece en la base de datos");
				throw new ServicioException(20212, "Transacción no aparece en la base de datos");
			}
		} catch (TipoDatoException | ParseException e) {
			logger.error("Error al recuperar objeto peticion: {}",e.getMessage(),e);
			throw new ServicioException(20001, "Error al recuperar objeto peticion.");
		}
	}

	
	private void reversaPagoPrestamo(DatosOperacion datos) throws ServicioException {

		ReversaPagoPrestamoPeticion peticion;
		CuentaPrestamo pcp;
		CuentaPrestamo pcpResp;
		try {
			peticion = datos.obtenerObjeto("peticion",ReversaPagoPrestamoPeticion.class);
			pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			List<Long> glbDtimeAAATRList = (List<Long>) datos.obtenerValor("glbDtimeAAATRList");
			
			for (Long glbDtime : glbDtimeAAATRList) {
				Object[] paramsAAATR = {
						Constantes.SI,
						glbDtime
				};
				logger.debug("Colocando la bandera de reversa a valor SI, en la AAATR");
				ejecutarSentencia(query(UPDATE_SFBDB_AAATR), paramsAAATR);
			}
			
			logger.debug("Procesar Reversa");
			BigDecimal valorReversa = peticion.getValorMovimiento();
			BigDecimal saldoTercerosDiaHoyPcp = pcp.getSaldoTercerosDiaHoy();
			pcp.setSaldoTercerosDiaHoy(saldoTercerosDiaHoyPcp.add(valorReversa));
			datos.agregarDato("pcp", pcp);
			datos.agregarDato("valorReversa", valorReversa);

			/// AGREGAR OBTENER EL VALOR TERCEROS DE LA NOTA BANCARIA
			

			Object[] paramsPPANB = {
			pcp.getCodProducto(),
			pcp.getCodOficina(),
			pcp.getNumCuenta(),
			Constantes.ISPEC_PP210,
			datos.obtenerInteger("fechaPago"),
			peticion.getNumDocumentoTran()		
			};
			
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT_PPANB_VALOR_TERCERO, parametros: " + Arrays.toString(paramsPPANB));
			BigDecimal valorTercerosPPANB = jdbcTemplate.queryForObject(query(SELECT_PPANB_VALOR_TERCERO), BigDecimal.class, paramsPPANB);
			
			if (UtileriaDeDatos.isGreater(valorTercerosPPANB, BigDecimal.ZERO)) {
				pcp.setSaldoTercerosDiaHoy(pcp.getSaldoTercerosDiaHoy().add(valorTercerosPPANB));
				valorReversa = valorReversa.add(valorTercerosPPANB);
				datos.agregarDato("valorReversa", valorReversa);
			}
			
		//	
			
			revertirPagoPrestamo(datos);
			//
			
			//pcp.setSaldoTercerosDiaHoy(pcp.getSaldoTercerosDiaHoy().add(valorTercerosPPANB));
			//
			pcpResp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			
			pcp.setSaldoReal(pcpResp.getSaldoReal());
			pcp.setSaldoTercerosDiaHoy(pcpResp.getSaldoTercerosDiaHoy());
			pcp.setSaldoVencido(pcpResp.getSaldoVencido());
			pcp.setSaldoTotalMes(pcpResp.getSaldoTotalMes());
			pcp.setSumAjusteDevengInteresCompensa(pcpResp.getSumAjusteDevengInteresCompensa());
			pcp.setSumInteresCompensaDevengado(pcpResp.getSumInteresCompensaDevengado());
			pcp.setSumAjusteDevengoInteresMora(pcpResp.getSumAjusteDevengoInteresMora());
			pcp.setSumInteresMoraDevengado(pcpResp.getSumInteresMoraDevengado());
			pcp.setInteresMoraPagadoHoy(pcpResp.getInteresMoraPagadoHoy());
			pcp.setSumInteresPagadoHoy(pcpResp.getSumInteresPagadoHoy());
			pcp.setSumInteresCompensaPagado(pcpResp.getSumInteresCompensaPagado());
			pcp.setSumInteresCompensaPagadoHoy(pcpResp.getSumInteresCompensaPagadoHoy());
			pcp.setSumCapitalPagadoHoy(pcpResp.getSumCapitalPagadoHoy());
			pcp.setSumInteresPagadoHastaUltimo(pcpResp.getSumInteresPagadoHastaUltimo());
			pcp.setSumInteresReversadoMes(pcpResp.getSumInteresReversadoMes());
			pcp.setSumInteresCompensaReversMesAnt(pcpResp.getSumInteresCompensaReversMesAnt());
			pcp.setSumInteresMoraPagadoMes(pcpResp.getSumInteresMoraPagadoMes());
			pcp.setSumInteresPagadoMes(pcpResp.getSumInteresPagadoMes());
			pcp.setSumInteresMoraPagadoMesAnt(pcpResp.getSumInteresMoraPagadoMesAnt());
			pcp.setSumInteresPagadoMesAnterior(pcpResp.getSumInteresPagadoMesAnterior());
			pcp.setInteresMoraPagado(pcpResp.getInteresMoraPagado());
			pcp.setSumInteresMoraReversadoMes(pcpResp.getSumInteresMoraReversadoMes());
			pcp.setSumInteresMoraReversadoMesAnt(pcpResp.getSumInteresMoraReversadoMesAnt());
			pcp.setCodBaseCalculoMora(pcpResp.getCodBaseCalculoMora());
			pcp.setSumInteresDevengadoMes(pcpResp.getTasaInteresMora());
			pcp.setCodBaseCobroCompensatorio(pcpResp.getTasaAnualMora().intValue());
			
			datos.agregarDato("pcp", pcp);
			anularFactura(datos);
			pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class); //Recuperando objecto cuenta prestamo de funcion interna
			
			Integer cantidadCuotasAdelantadasPPAFA = pcp.getCantidadCuotasAnticipadas()
													- datos.obtenerInteger("cantidadCuotasAdelantadasPPAFA");
			actualizarFechaFactura(pcp);
			logger.debug("Actualizando maestro de préstamos");
			
			Integer fechaVencimientoPPAFA = datos.obtenerInteger("fechaVencimientoPPAFA");
			BigDecimal tasaInteresNormalPPAFA = datos.obtenerBigDecimal("tasaInteresNormalPPAFA");
			Integer fechaProximaFacturacionPPAFA = datos.obtenerInteger("fechaProximaFacturacionPPAFA");
			Integer fechaVencimientoCapitalPPAFA = datos.obtenerInteger("fechaVencimientoCapitalPPAFA");
			
			if (UtileriaDeDatos.isEquals(fechaVencimientoPPAFA, new Integer(0))) {
				fechaVencimientoPPAFA = pcp.getFechaProximoVencimientoFactura();
				datos.agregarDato("fechaVencimientoPPAFA", fechaVencimientoPPAFA);
			}
			
			if (UtileriaDeDatos.isEquals(tasaInteresNormalPPAFA, BigDecimal.ZERO)) {
				tasaInteresNormalPPAFA = pcp.getTasaInteresNormal();
				datos.agregarDato("tasaInteresNormalPPAFA", tasaInteresNormalPPAFA);
				}
			
			if (UtileriaDeDatos.isEquals(fechaProximaFacturacionPPAFA, new Integer(0))) {
				fechaProximaFacturacionPPAFA = pcp.getFechaProximaFacturacion();
				datos.agregarDato("fechaProximaFacturacionPPAFA", fechaProximaFacturacionPPAFA);
				}
			
			if (UtileriaDeDatos.isEquals(fechaVencimientoCapitalPPAFA, new Integer(0))) {
				fechaVencimientoCapitalPPAFA = pcp.getFechaVencimientoCapital();
				datos.agregarDato("fechaVencimientoCapitalPPAFA", fechaVencimientoCapitalPPAFA);
				}
			
			Object[] paramsPPMPR = {
					pcp.getSaldoUltimaFacturacion(),
					pcp.getSenPrimerPeriodo(),
					pcp.getPeriodoPagoInteres(),
					pcp.getCantidadDiasProximoVencimiento(),
					datos.obtenerInteger("fechaVencimientoCapitalPPAFA"),
					datos.obtenerInteger("fechaVencimientoPPAFA"),
					datos.obtenerInteger("fechaProximaFacturacionPPAFA"),
					datos.obtenerBigDecimal("tasaInteresNormalPPAFA"),
					pcp.getTasaInteresCompensatorio(),
					pcp.getTasaInteresMora(),
					cantidadCuotasAdelantadasPPAFA,
					pcp.getGlbDtime()
			};
			
			logger.debug("Ejecutando sentencia UPDATE LINC SFBDB LCMLC, parametros: " + Arrays.toString(paramsPPMPR));
			ejecutarSentencia(query(UPDATE_SFBDB_PPMPR), paramsPPMPR);
			
			logger.debug("Calculo de intereses normal, compensatorio, mora, capital vencido"); 
			String codDestinoPcp			= StringUtils.leftPad(pcp.getCodDestino().toString(), 5, '0');
			String codEstadoPrestamoPcp		= String.valueOf(pcp.getCodEstadoPrestamo());
			Object[] paramsBSMTG = {
					Constantes.PP_DESTINO_FONDOS,
					codDestinoPcp
				};
			
			String destinoPrestamo = jdbcTemplate.queryForObject(query(SELECT_DEST_PRESTAMO), String.class, paramsBSMTG);
			datos.agregarDato("destinoPrestamo", destinoPrestamo);
			
			Object[] paramsBSMTG2 = {
					"ESTPRESTAM",
					codEstadoPrestamoPcp
				};
			
			String nombreEstadoPrestamo = jdbcTemplate.queryForObject(query(SELECT_NOM_PRESTAMO), String.class, paramsBSMTG2);
			datos.agregarDato("nombreEstadoPrestamo", nombreEstadoPrestamo);
			
			pcp.setCantidadFacturasEmitidasNormal(pcp.getCantidadFacturasEmitidasNormal() - 1);
			calcularVariablesVencimiento(datos);
			
			Integer fechaUltimoPago = datos.obtenerInteger("fechaUltimoPago");
			
			pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class); //Recuperando objecto cuenta prestamo de funcion interna
			Object[] paramsFactura = {
					pcp.getCodProducto(),
					pcp.getCodOficina(),
					pcp.getNumCuenta()					
				};
			logger.debug("Actualizando maestro de préstamos SFBDB_PPMPR");
			Integer ultimaFactura=0;
			try {  ultimaFactura= jdbcTemplate.queryForObject(query(SELECT_ULTIMA_FACTURA), Integer.class, paramsFactura);}
			catch 
			(EmptyResultDataAccessException erdae) {  } 
			
			
			
			pcp.setNumFacturaImpagada(pcp.getNumFactura()+1);
			Object[] paramsPPMPR2 = {
					//datos.obtenerBigDecimal("sumInteresDevengadoNoFacturadoV"),
					pcp.getSumInteresDevengadoNoFacturado(),
					pcp.getSumCapitalPagadoHoy(),
					pcp.getSumCapitalPagadoMes(),
					pcp.getSumCapitalPagadoMesAnterior(),
					pcp.getSumCapitalReversadoMes(),
					pcp.getSumCapitalReversadoMesAnterior(),
					pcp.getInteresMoraPagadoHoy(),
					pcp.getInteresMoraPagado(),
					pcp.getSumInteresMoraReversadoMes(),
					pcp.getSumInteresMoraDevengado(),
					pcp.getSumAjusteDevengoInteresMora(),
					pcp.getInteresMoraCondonado(),
					pcp.getTotalInteresMoraCondonado(),
					pcp.getSumInteresCompensaPagadoHoy(),
					pcp.getSumInteresCompensaPagado(),
					pcp.getSumInteresCompensaReversadoMes(),
					pcp.getSumInteresCompensaReversMesAnt(),
					pcp.getSumInteresCompensaDevengado(),
					pcp.getSumAjusteDevengInteresCompensa(),
					pcp.getSumInteresCompensaCondonadoHoy(),
					pcp.getTotalInteresCompensaCondonado(),
					pcp.getFechaAcumulacionDevengo(),
					fechaUltimoPago,
					pcp.getNumFactura(),
					pcp.getSaldoTeorico(),
					pcp.getSaldoReal(),
					pcp.getSaldoVencido(),
					pcp.getSaldoTercerosDiaHoy(),
					pcp.getSaldoTercerosDiaSiguiente(),
					pcp.getCantidadFacturasEmitidasNormal(),
					datos.obtenerInteger("cantidadCuotasPagadas"), //Valor recuperado en funcion interna calcularVariablesVencimiento
					pcp.getNumFacturaImpagada(),
					pcp.getFechaVencimientoImpago(),
					pcp.getCantDiasDesplazaVencHabiles(),
					pcp.getFechaProxFacturacionProrroga(),
					pcp.getSumMoraProrrogada(),
					pcp.getSaldoTotalMes(),
					datos.obtenerInteger("fechaPago"),
					pcp.getFechaVencimientoImpago(),
					pcp.getFechaVencimientoImpago(),
					datos.obtenerBigDecimal("saldoInteresNormalVencido"),
					datos.obtenerBigDecimal("saldoMoraVencido"),
					datos.obtenerBigDecimal("saldoCompensatorioVencido"),
					pcp.getSumInteresPagadoHoy(),
					pcp.getSumInteresPagadoHastaUltimo(),
					new Integer(1),
					pcp.getGlbDtime()
			};
			logger.debug("Ejecutando sentencia UPDATE SFBDB PPMPR2, parametros: {}", Arrays.toString(paramsPPMPR2));
			ejecutarSentencia(query(UPDATE_SFBDB_PPMPR2), paramsPPMPR2);
			
			
			
		} catch (TipoDatoException | ParseException e) {
			logger.error("Error inesperado al recuperar datos: {}",e.getMessage(),e);
			throw new ServicioException(20001, "Error inesperado al recuperar datos");
			
		}
	}
	
	private void registrarEventoYNotaPrestamo(DatosOperacion datos) throws ServicioException {
		ReversaPagoPrestamoPeticion peticion;
		CuentaPrestamo pcp;
		
		try {
			peticion = datos.obtenerObjeto("peticion",ReversaPagoPrestamoPeticion.class);
			pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			
			Integer fechaSistema = datos.obtenerInteger("fechaSistema");
			Date fecha6;
			try {
				fecha6 = UtileriaDeDatos.fecha6ToDate(fechaSistema);
			} catch (ParseException e) {
				logger.error("Error al recuperar objeto peticion: {}",e.getMessage(),e);
				throw new ServicioException(20001, "Error al recuperar objeto peticion.");
			}
			Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fecha6);
			
			logger.debug("Registrando evento del prestamo");
			datos.agregarDato("cuentaPrestamo", peticion.getCuentaPrestamo());
			datos.agregarDato("codCausal", Constantes.PP_CAUSAL_PRESTAMO);
			datos.agregarDato("codPantalla", Constantes.ISPEC_PP210);
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codDestinoFondos", new Integer(0));
			datos.agregarDato("codOrigenFondos", new Integer(0));
			datos.agregarDato("codAccion", " ");
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("numResolucion", " ");
			datos.agregarDato("numActaResolucion", " ");
			datos.agregarDato("tasaAnualInteresAnticipado", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresCompensatorio", pcp.getTasaAnualInteresCompensatorio());
			datos.agregarDato("tasaAnualInteresMoratorio", pcp.getTasaAnualMora());
			datos.agregarDato("tasaAnualInteresVencido", pcp.getTasaAnualInteresNormal());
			datos.agregarDato("codUsuarioActual", " ");
			datos.agregarDato("codUsuarioAnterior", " ");
			datos.agregarDato("codModificacion", new Integer(0));
			datos.agregarDato("valorModificacion", " ");
			datos.agregarDato("senReversa", Constantes.SI);
			datos.agregarDato("fechaReversa", fechaSistemaAMD);
			registrarEventosPrestamo(datos);
			
			
			logger.debug("Generando nota del prestamo");
			datos.agregarDato("cuentaPrestamo", peticion.getCuentaPrestamo());
			datos.agregarDato("cuentaRelacionada", " ");
			datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);
			datos.agregarDato("codCausal", Constantes.PP_CAUSAL_PRESTAMO);
			datos.agregarDato("codSubCausal", new Integer(0));
			datos.agregarDato("codPantalla", Constantes.ISPEC_PP210);//TODO Validar comentario hecho en el documento de disenio.
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
			datos.agregarDato("tasaAnualInteresAnticipado", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresCompensatorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresMoratorio", BigDecimal.ZERO);
			datos.agregarDato("tasaAnualInteresVencido", BigDecimal.ZERO);
			datos.agregarDato("valorInteresAnticipado", BigDecimal.ZERO);
			datos.agregarDato("valorCapitalAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorMoraAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorMovimiento", BigDecimal.ZERO);
			datos.agregarDato("valorGastosAjenosFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorGastosPropiosFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorInteresCompensatorioFacturados", BigDecimal.ZERO);
			datos.agregarDato("valorInteresVencidoAfectado", BigDecimal.ZERO);
			datos.agregarDato("valorSeguroDanios", BigDecimal.ZERO);
			datos.agregarDato("valorOtrosSeguros", BigDecimal.ZERO);
			datos.agregarDato("valorSeguroVida", BigDecimal.ZERO);
			datos.agregarDato("numDocumentoTran", new Integer(0));
			datos.agregarDato("senReversa", Constantes.SI);
			registrarNotasPrestamo(datos);
			
			//Agregando valorMovimiento a objeto datos para que sea retornado en la respuesta del servicio
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			
		} catch (TipoDatoException e) {
			logger.error("Error al recuperar objeto peticion: {}",e.getMessage(),e);
			throw new ServicioException(20001, "Error al recuperar objeto peticion.");
		}
		
		
	}
	private List<Map<String, Object>> recuperaFacturasPagadas1(CuentaPrestamo pcp) throws TipoDatoException{
		
		Object[] paramsPPMFA = {
				pcp.getCodOficina(),
				pcp.getCodProducto(),
				pcp.getNumCuenta(),
				Constantes.PP_ESTADO_FACTURA_PARCIAL,
				Constantes.PP_ESTADO_FACTURA_IMPAGA
				
		};

		logger.debug("Estableciendo consultas para recuperar facturas pagadas");
		List<Map<String, Object>> facturasPagadas = jdbcTemplate.queryForList(query(SELECT_SFBDB_PPMFA_FACT_PAGADA2), paramsPPMFA);
		return facturasPagadas;
	}
    private void actualizarFechaFactura (CuentaPrestamo pcp) throws TipoDatoException 
    {			
    List<Map<String, Object>> listaFactPagadas1 = recuperaFacturasPagadas1(pcp);

	for (Map<String, Object> map : listaFactPagadas1) {

		AdaptadorDeMapa adaptadorMapa = UtileriaDeDatos.adaptarMapa(map);
		
		pcp.setFechaVencimientoImpago(adaptadorMapa.getInteger("fechaVencimientoFactura"));

	}
}
	private void procesarChequesPropiosYRetenciones(DatosOperacion datos) throws ServicioException {
		try {
			ReversaPagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", ReversaPagoPrestamoPeticion.class);
			
			//Recuperando numTran del servicio en este paso para no perder valor,
			//porque se sustituye en FS Pagos cheques, y se setea al final
			Integer numTran = datos.obtenerInteger("numTran");
			
			CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			if (!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques())) {
				for(Cheque chk: peticion.getCheques()) {
					if(UtileriaDeDatos.isEquals(chk.getTipCheque(), 1)) {
						
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
						chk.setNumTran(datos.obtenerInteger("numTran"));
						chk.setCodCajero(datos.obtenerString("codCajero"));
						chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));
						chk.setNomOficina(datos.obtenerString("nomOficina"));
						
					}else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 3) ||
							 UtileriaDeDatos.isEquals(chk.getTipCheque(), 4)){
						
						datos.agregarDato("cuentaDestino", peticion.getCuentaPrestamo());
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
						datos.agregarDato("codCompania", pcp.getCodCompania());
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
			}
			datos.agregarDato("cheques", peticion.getCheques());
			datos.agregarDato("numTran", numTran);
			
		} catch (TipoDatoException e) {
			logger.error("Error inesperado:", e.getMessage(), e);
			throw new ServicioException(20001, "Error");
		}
		
	}
}

