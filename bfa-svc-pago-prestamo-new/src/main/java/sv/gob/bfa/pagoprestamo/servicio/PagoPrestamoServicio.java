package sv.gob.bfa.pagoprestamo.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import sv.gob.bfa.core.fs.FSAplicarAbonoExtra;
import sv.gob.bfa.core.fs.FSCalcularDevengo;
import sv.gob.bfa.core.fs.FSCalcularDevengoRubroPrestamo;
import sv.gob.bfa.core.fs.FSCalcularVariablesVencimiento;
import sv.gob.bfa.core.fs.FSValorEnLetras;
import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.model.CuentaPrestamo;
import sv.gob.bfa.core.model.RealizarPagoFacturaRespuesta;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.Servicio;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;

import sv.gob.bfa.pagoprestamo.model.PagoPrestamoPeticion;
import sv.gob.bfa.pagoprestamo.model.PagoPrestamoRespuesta;

/**
 * Proveer de un servicio que permita el registro del pago a cuenta préstamo en valores de efectivo, cheques propios y ajenos.
 */
public class PagoPrestamoServicio extends Servicio{
	
	private static final String NOM_COD_SERVICIO = "Pago Prestamo PP210: ";
	
	private static final String SELECT_LINC_SFBDB_PPRGP = 
			"SELECT COUNT(*) AS cantidadGastos" + 
			"	FROM LINC.SFBDB_PPRGP@DBLINK@" + 
			"	WHERE PCU_OFICI = ?" + 
			"     AND PCU_PRODU = ?" + 
			"     AND PCUNUMCUE = ?" + 
			"     AND (PCO_GASTO = ?" + 
			"         OR PCO_GASTO = ?" + 
			"         OR PCO_GASTO = ?)" + 
			"     AND PFEINIAPL >= ?" + 
			"	  AND PCO_ESTAD = ?";
	
	private static final String SELECT_LINC_SFBDB_PPRGP1 = 
			"SELECT SUM(PVA_GASTO) AS sumGastosCovid" + 
			"	FROM LINC.SFBDB_PPRGP@DBLINK@" + 
			"	WHERE PCU_OFICI = ?" + 
			"     AND PCU_PRODU = ?" + 
			"     AND PCUNUMCUE = ?" + 
			"     AND (PCO_GASTO = 191" + 
			"         OR PCO_GASTO = 192" + 
			"         )" + 
			"	  AND PCO_ESTAD = ?";
	
	private static final String SELECT_LINC_SFBDB_AAATR = 
			"SELECT COUNT(*) AS cantidadTransaccion" + 
			"	FROM LINC.SFBDB_AAATR@DBLINK@" + 
			"	WHERE 	TFETRAREL = ?" + 
			"		AND DCO_OFICI = ?" + 
			"		AND DCO_TERMI = ?" + 
			"    	AND TNU_TRANS >= ?" + 
			"    	AND DCO_TRANS = ?" + 
			"    	AND ACU_PRODU = ?" + 
			"    	AND ACU_OFICI = ?" + 
			"    	AND ACUNUMCUE = ?" + 
			"    	AND ACUDIGVER = ?" + 
			"    	AND TNUDOCTRA = ?"  
	//		"    	AND TVA_MOVIM = ?" + 
			//"    	AND TSE_REVER <> ?"
			;
	
	private static final String SELECT_LINC_SFBDB_PPMFA = 
			"SELECT PVA_CAPIT AS valorCapital," + 
			"		PVAPAGCAP AS valorCapitalPagado," + 
			"    	PVA_INTER AS valorInteresNormalFacturado," + 
			"    	PVAPAGINT AS valorInteresNormalPagado," + 
			"    	PVAINTEXT AS valorInteresCompenFacturado," + 
			"    	PVAPAGVEN AS valorInteresCompenPagado," + 
			"		PVAPAGMOR AS valorInteresMoraPagado," + 
			"		PVA_MORA  AS valorInteresMoraFacturado," + 
			"		PVAGASEXT AS valorGastoAjenoFacturado," + 
			"		PVAGASPRO AS valorGastoPropioFacturado," + 
			"		PVAPAGPRO AS valorGastoPropioPagado," + 
			"		PVAPAGEXT AS valorGastoAjenoPagado," + 
			"		PFE_VENCI AS fechaVencimientoFactura" + 
			"	FROM LINC.SFBDB_PPMFA@DBLINK@" + 
			"	WHERE 	PCU_OFICI = ?" + 
			"		AND PCU_PRODU = ?" + 
			"    	AND PCUNUMCUE = ?" + 
			"    	AND (PCO_ESTAD = ?" + 
			"                       OR " + 
			"           PCO_ESTAD = ?)"
			;
	
	private static final String SELECT_LINC_SFBDB_PPMFA_1 = 
			"SELECT PVA_CAPIT AS valorCapital," + 
			"    	PVA_INTER AS valorInteresNormalFacturado," + 
			"    	PVAGASPRO AS valorGastoPropioFacturado," + 
			"    	PVAGASEXT AS valorGastoAjenoPagado," + 
			"		PFE_VENCI AS fechaVencimientoFactura" + 
			"	FROM LINC.SFBDB_PPMFA@DBLINK@" + 
			"	WHERE	PCU_OFICI = ?" + 
			"		AND PCU_PRODU = ?" + 
			"    	AND PCUNUMCUE = ?" + 
			"    	AND PNU_FACTU > ?" + 
			"    	AND PFE_VENCI > ?" + 
			"    	AND (PCO_ESTAD = ? " + 
			"    	OR PCO_ESTAD = ?)"
			;
	
	private static final String SELECT_DESTINO_PRESTAMO = 
			"SELECT ANO_CORTA AS destinoPrestamo" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE	ACO_TABLA = ?" + 
			"    	AND ACO_CODIG = ?"
			;
	
	private static final String SELECT_NOMBRE_ESTADO_PRESTAMO = 
			"SELECT ANO_CORTA AS nombreEstadoPrestamo" + 
			"	FROM LINC.SFBDB_BSMTG@DBLINK@" + 
			"	WHERE	ACO_TABLA = ?" + 
			"		AND ACO_CODIG = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_PPMPR = 
			"UPDATE LINC.SFBDB_PPMPR@DBLINK@" + 
			" SET" + 
			"    PFEPRXFAC = ?,"+		
			"    PSUINTFAC = ?, PSUCAPPHO = ?," + 
			"    PSUCAPPME = ?, PSUCAPPAN = ?," + 
			"    PSUCAPRME = ?, PSUCAPRAN = ?," + 
			"    PSUMORPHO = ?, PSUMORPCR = ?," + 
			"    PSUMORRME = ?, PSUMORDEV = ?," + 
			"    PSUMORAJD = ?, PSUMORCON = ?," + 
			"    PSUMORCCR = ?, PSUEXTPHO = ?," + 
			"    PSUEXTPCR = ?, PSUEXTRME = ?," + 
			"    PSUEXTRAN = ?, PSUEXTDEV = ?," + 
			"    PSUEXTAJD = ?, PSUEXTCON = ?," + 
			"    PSUEXTCCR = ?, PFEACUDEV = ?," + 
			"    PFEULTPAG = ?, PNU_FACTU = ?," + 
			"    PSA_TEORI = ?, PSA_REAL  = ?," + 
			"    PSA_VENCI = ?, PSATERHOY = ?," + 
			"    PSATERDSI = ?, PCN_PAGOS = ?," + 
			"    PCNCUOPAG = ?, PFEVENIMP = ?," + 
			"    PDIDESVEN = ?, PFEPRXPRO = ?," + 
			"    PSUMORPRO = ?, PSATOTMES = ?," + 
			"    PFEPRXEVE = ?, PFEVENIMPI = ?," + 
			"    PFEVENIMPK = ?, PSAINTVEN = ?," + 
			"    PSAMORVEN = ?, PSACOMVEN = ?," + 
			"	 PSUINTAJD = ?, PSUINTDEV = ?," + 
			"	 PCN_TRANS = PCN_TRANS + ?," + 
			"	 PNUFACIMP = ?," + 
			"	 PSUINTPHO = ?," + 
			"	 PSUINTPCR = ?" + 
			" WHERE GLB_DTIME = ?";
	
	private static final String SELECT_MADMIN_FNC_CORREL_CANAL = "SELECT MADMIN.FNC_CORREL_CANAL(?) as numTran FROM DUAL";
	
	private static final String SELECT_LINC_SFBDB_AAMCO = 
			"SELECT ACO_CAUPA AS codCausal" + 
			"	FROM LINC.SFBDB_AAMCO@DBLINK@" + 
			"	WHERE ACO_COMPA = ?";
	
	private static final String UPDATE_LINC_SFBDB_AAARP = 
			"UPDATE LINC.SFBDB_AAARP@DBLINK@" + 
			"   SET " 				 +
			"		ACO_CAUSA = ?, " + 
			"       ACO_CONCE = ?," + 
			"       ACU_OFICI = ?," + 
			"       ACU_PRODU = ?," + 
			"       ACUNUMCUE = ?," + 
			"       ACUDIGVER = ?," + 
			"       DCOTERADI = ?," + 
			"       DCOTERUSO = ?," + 
			"       SCO_ESTAD = ?," + 
			"       SCOOFIUSO = ?," + 
			"       SCOUSUUSO = ?," + 
			"       SFE_USO   = ?," + 
			"       SHO_USO   = ?," + 
			"       TNUDOCTR2 = ?," + 
			"       TNUDOCTRA = ?," + 
			"       TVA_EFECT = ?," + 
			"       TVA_MOVIM = ?," + 
			"       TVA_VALOR = ?," + 
			"       TNU_TRANS = ?" + 
			" WHERE GLB_DTIME = ?"
			;
	
	private static final String SELECT_MONTO_DEUDA = 
			"SELECT SUM(MONTODEUDA) " + 
			"	FROM (SELECT SUM((FA1.PVA_CAPIT - FA1.PVAPAGCAP)+ " + 
			"                 (FA1.PVA_INTER - FA1.PVAPAGINT)+ " + 
			"                 (FA1.PVAINTEXT - FA1.PVAPAGVEN)+ " + 
			"                 (FA1.PVA_MORA  - FA1.PVAPAGMOR)+ " + 
			"                 (FA1.PVAGASPRO - FA1.PVAPAGPRO)+ " + 
			"                 (FA1.PVAGASEXT - FA1.PVAPAGEXT)) AS MONTODEUDA" + 
			"         FROM  LINC.SFBDB_PPMFA@DBLINK@ FA1" + 
			"         WHERE FA1.PCU_PRODU = ? AND FA1.PCU_OFICI = ? AND FA1.PCUNUMCUE = ?" + 
			"         AND   FA1.PCO_ESTAD IN (?,?)" + 
			"         AND   FA1.PFE_VENCI <= ?" + 
			"	UNION ALL" + 
			"	SELECT   NVL(PSA_TEORI,0) AS MONTODEUDA" + 
			"		FROM  LINC.SFBDB_PPMPR@DBLINK@  " + 
			"		WHERE PCU_PRODU = ? AND PCU_OFICI = ? AND PCUNUMCUE = ?"+
			"	UNION ALL" + 
			"	SELECT   NVL(PSUINTFAC,0) AS MONTODEUDA" + 
			"		FROM  LINC.SFBDB_PPMPR@DBLINK@  " + 
			"		WHERE PCU_PRODU = ? AND PCU_OFICI = ? AND PCUNUMCUE = ?)"
			;
	
	private static final String SELECT_NOM_COMPANIA = 
			"SELECT ANO_COMPA AS  nomCompania" + 
			"	FROM LINC.SFBDB_AAMCO@DBLINK@" + 
			"		WHERE ACO_COMPA = ?";
	
	/**
	 * Proveer de un servicio que permita el registro del pago a cuenta préstamo en valores de efectivo, cheques propios y ajenos.
	 */
	@Transactional("transactionManager")
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {
		
		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");
		
		try {
			
			logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion...");
			DatosOperacion datos = crearDatosOperacion();
			
			logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> PagosChequesGerenciaPeticion ");
			PagoPrestamoPeticion peticion = (PagoPrestamoPeticion) objetoDom;
		
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			validacionInicial(peticion);
			datos.agregarDato("peticion", peticion);
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
			datos.agregarDato("InteresPendienteNoFacturado", pcp.getSumInteresDevengadoNoFacturado());
			datos.agregarDato("pcp", pcp);
			datos.agregarDato("codDestino", pcp.getCodDestino());
			datos.agregarDato("numeroCarpetaCliente", pcp.getNumeroCarpetaCliente());
			datos.agregarDato("tecnicoCuenta", pcp.getTecnicoCuenta());
			datos.agregarDato("tasaAnualInteresNormal", pcp.getTasaAnualInteresNormal());
			datos.agregarDato("codEstadoPrestamo", pcp.getCodEstadoPrestamo());
			datos.agregarDato("codBloqueo",pcp.getCodBloqueo());
			validarEstadoPrestamos(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar que el pago no exceda el valor de la deuda total");
			validarPagoNoExcedeDeuda(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Creando objeto auxiliar 'Cliente' con codigo cliente -> {} ", pcp.getCodCliente());
			Cliente cliente = recuperarDatosCliente(pcp.getCodCliente());
			datos.agregarDato("cliente", cliente);
			datos.agregarPropiedadesDeObjeto(cliente);
			
			logger.debug(NOM_COD_SERVICIO + "Validaciones de la transaccion de pago de prestamo.");
			validacionDeTransaccion(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validacion de registro de formulario UIF.");
			//Agregando parametros de entrada para funcion de soporte
			datos.agregarDato("numTransLavado", peticion.getNumTransLavado());
			datos.agregarDato("tipDocumentoPersona", peticion.getTipDocumentoPersona());
			datos.agregarDato("numDocumentoPersona", peticion.getNumDocumentoPersona());
			datos.agregarDato("nombrePersona", peticion.getNombrePersona());
			datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
			datos.agregarDato("valorCheques", peticion.getValorCheques());
			datos.agregarDato("ofiAdministrativa",pcp.getOfiAdministrativa());
			validarRegistroUIF(datos);
			
			validacionMontos(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Calcular valores a imprimir en el recibo de pago.");
			calcularValoresRecibo(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Realizar el pago del prestamo.");
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			realizarPagoPrestamo(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Registro de la transacciones...");
			registroTransacciones(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Actualizacion de registro de lavado de dinero UIF...");
			actualizarRegistroUIF(datos);
			
			//procesarChequesPropiosRetencionesGerencia(datos);
			procesarCheques(datos);
			logger.debug(NOM_COD_SERVICIO + "Preparando objeto de respuesta ...");
			
			PagoPrestamoRespuesta respuesta = new PagoPrestamoRespuesta();
			datos.llenarObjeto(respuesta);
			datosRespuesta(datos, respuesta);
			
			respuesta.setCodigo(0);
			respuesta.setDescripcion("EXITO");
			
			if(logger.isDebugEnabled()) {
				logger.debug(NOM_COD_SERVICIO + "RESPUESTA: {} ", respuesta);
			}
			
			return respuesta;
		} catch (ServicioException se){
			logger.error("Ocurrio un error inesperado:", se.getMessage(), se);
			throw manejarMensajeExcepcionServicio(se);
		} catch (TipoDatoException | ParseException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, "Error inesperado: " + e.getMessage()));
		}
	}

	
	private void datosRespuesta(DatosOperacion datos, PagoPrestamoRespuesta respuesta) throws TipoDatoException {
		
		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		respuesta.setValorMovimiento(peticion.getValorMovimiento());
		respuesta.setValorEfectivo(peticion.getValorEfectivo());
		respuesta.setValorCheques(peticion.getValorCheques());
		respuesta.setValorChequesAjenos(peticion.getValorChequesAjenos());
		respuesta.setValorChequesPropios(peticion.getValorChequesPropios());
		
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


	private void procesarCheques(DatosOperacion datos) throws TipoDatoException, ServicioException {



		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		Cliente cliente = datos.obtenerObjeto("cliente",Cliente.class);
		String codPantalla = "";
		codPantalla	= datos.obtenerString("codPantalla");		

		//Recuperando numTran del servicio en este paso para no perder valor,

		//porque se sustituye en FS Pagos cheques, y se setea al final

		Integer numTran = datos.obtenerInteger("numTran");

		

		if (!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques())) {	
			for (Cheque chk : peticion.getCheques()) {
				if (
					UtileriaDeDatos.isEquals(chk.getTipCheque(), new Integer(1)) ||
					UtileriaDeDatos.isEquals(chk.getTipCheque(), 2)
					) {

					//pagoCheque

					

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

					//codCompania

		            //fechaSistema

		            //fechaRelativa

		            //fechaReal

		            //horaSistema

					datos.agregarDato("senSupervisor", peticion.getSenSupervisor());

					pagosCheques(datos);

					chk.setNumTran(datos.obtenerInteger("numTran"));

					chk.setCodCajero(datos.obtenerString("codCajero"));

					chk.setCodOficinaTran(datos.obtenerInteger("codOficinaTran"));

					chk.setNomOficina(datos.obtenerString("nomOficina"));

				} 
			      else if (UtileriaDeDatos.isEquals(chk.getTipCheque(), 3) ||
					 UtileriaDeDatos.isEquals(chk.getTipCheque(), 4)){

					//retenciones

					

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

					datos.agregarDato("nomOficina", datos.obtenerString("nomOficinaTran"));

		            //codCompania

		            //fechaSistema

		            //fechaRelativa

		            //fechaReal

		            //horaSistema

					

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

		}

		//Recuperando valor de parametro de salida numDocumentoTran

		datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
		datos.agregarDato("codPantalla", codPantalla);
		datos.agregarDato("cheques", peticion.getCheques());

		datos.agregarDato("numTran", numTran);
		datos.agregarDato("codCliente", cliente.getCodCliente());
		datos.agregarDato("nombreCompletoCliente",cliente.getNombreCompletoCliente());

	}




	private void actualizarRegistroUIF(DatosOperacion datos) throws TipoDatoException, ParseException, ServicioException {
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		Date fechaSistemaDMA = UtileriaDeDatos.fecha6ToDate(fechaSistema);
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistemaDMA);
		
		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		if (UtileriaDeDatos.isGreater(peticion.getNumTransLavado(), new Integer(0))) {
			
			Object[] paramsAAARP = {
				datos.obtenerValor("codCausal"), 
				datos.obtenerValor("codConcepto"), 
				pcp.getCodOficina(), 
				pcp.getCodProducto(),
				pcp.getNumCuenta(), 
				pcp.getDigitoVerificador(), 
				datos.obtenerInteger("rpCodTerminalReg"), 
				peticion.getCodTerminal(),
				Constantes.UIF_ESTADO_USADO, 
				peticion.getCodOficinaTran(), 
				peticion.getCodCajero(), 
				fechaSistemaAMD,
				datos.obtenerValor("horaSistema"), 
				new Integer(0), 
				peticion.getNumDocumentoTran(), 
				peticion.getValorEfectivo(),
				peticion.getValorMovimiento(), 
				peticion.getValorCheques(), 
				datos.obtenerValor("numTran"), 
				datos.obtenerLong("glbDtimeAAARP")
			};
			
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_AAARP), paramsAAARP);
		}
		
	}


	private void registroTransacciones(DatosOperacion datos) throws TipoDatoException, ParseException, ServicioException {
		//10. Registro de las transacciones...
		String descripcionTran	=	"TIPO DOC.:" + datos.obtenerInteger("tipDocumentoPersona") +
									"NUM DOC.:"  + datos.obtenerString("numDocumentoPersona") +
									"NOMBRE :"   + datos.obtenerString("nombrePersona");
		
		Integer codDebCre		= Constantes.CREDITO;
		Integer numTran			= jdbcTemplate.queryForObject(SELECT_MADMIN_FNC_CORREL_CANAL, Integer.class, Constantes.VENTANILLA);
		Integer codConcepto		= Constantes.CONCEPTO_PP;
		
		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		BigDecimal valorEfectivo	= peticion.getValorEfectivo();
		BigDecimal valorCheques		= peticion.getValorCheques();
		
		Integer senPosteo			= datos.obtenerInteger("senPosteo");
		String codCompa				= Constantes.COD_COMPANIA_BFA;
		
		datos.agregarDato("codCausalEfectivo", new Integer(0));
		if (!UtileriaDeDatos.isEquals(valorEfectivo, BigDecimal.ZERO)) {
			Integer codCausal				= Constantes.PP_CAUSAL_EFECTIVO;
			
			//Agregando parametro de salida
			datos.agregarDato("codCausalEfectivo", codCausal);
			
			//Validando parametros de entrada para funcion de soporte registro transaccion AAATR.
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("codConcepto", codConcepto);
			datos.agregarDato("codOficina", pcp.getCodOficina());
			datos.agregarDato("codProducto", pcp.getCodProducto());
			datos.agregarDato("numCuenta", pcp.getNumCuenta());
			datos.agregarDato("digitoVerificador", pcp.getDigitoVerificador());
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			//fechaRelativa ya esta por la seguridad
			datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("codCompania", codCompa);
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("numCaja", peticion.getNumCaja());
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codDebCre", codDebCre);
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numDocumentoReversa", new Integer(0));
			datos.agregarDato("saldoAnterior", BigDecimal.ZERO);
			datos.agregarDato("senAJATR", Constantes.NO);
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
			datos.agregarDato("senWANG", new Integer(0));
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.NO);
			datos.agregarDato("senPosteo", senPosteo);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", new BigDecimal(1));
			datos.agregarDato("valorMovimiento", peticion.getValorEfectivo());			
			datos.agregarDato("valorCheque", BigDecimal.ZERO);
			datos.agregarDato("valorVenta", new BigDecimal(1));
			datos.agregarDato("numDocumentoTran2", new Integer(0));
			datos.agregarDato("valorChequesAjenos", BigDecimal.ZERO);
			datos.agregarDato("valorChequesExt", BigDecimal.ZERO);
			datos.agregarDato("valorChequesPropios", BigDecimal.ZERO);
			datos.agregarDato("descripcionTran", descripcionTran);
			datos.agregarDato("numCuentaTransf", "0000000000000");
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("valorImpuesto",  datos.obtenerBigDecimal("saldoRealRecibo"));
			datos.agregarDato("tipDocumentoCliente", peticion.getTipDocumentoPersona());
			datos.agregarDato("numDocumentoCliente", peticion.getNumDocumentoPersona());
			datos.agregarDato("numDocumentoImp", new Integer(0));
			datos.agregarDato("codSubCausal", new Integer(0));
			
			registrarTransaccionAAATR(datos);
			
		}
		
		datos.agregarDato("codCausalCheque", new Integer(0));
		if (!UtileriaDeDatos.isEquals(valorCheques, BigDecimal.ZERO)) {
			Integer codCausal				= Constantes.PP_CAUSAL_CHEQUES;
			//Agregando parametro de salida
			datos.agregarDato("codCausalCheque", codCausal);
			//Validando parametros de entrada para funcion de soporte registro transaccion AAATR.
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("codConcepto", codConcepto);
			datos.agregarDato("codOficina", pcp.getCodOficina());
			datos.agregarDato("codProducto", pcp.getCodProducto());
			datos.agregarDato("numCuenta", pcp.getNumCuenta());
			datos.agregarDato("digitoVerificador", pcp.getDigitoVerificador());
			datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
			datos.agregarDato("codTerminal", peticion.getCodTerminal());
			//fechaRelativa ya esta por la seguridad
			datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
			datos.agregarDato("codCompania", codCompa);
			datos.agregarDato("codMoneda", pcp.getCodMoneda());
			datos.agregarDato("numCaja", peticion.getNumCaja());
			//montoIva -> null
			datos.agregarDato("montoIVA",datos.obtenerBigDecimal("saldoRealAnteriorRecibo"));
			datos.agregarDato("codTran", peticion.getCodTran());
			datos.agregarDato("codCajero", peticion.getCodCajero());
			datos.agregarDato("codDebCre", codDebCre);
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numDocumentoReversa", new Integer(0));
			datos.agregarDato("saldoAnterior", BigDecimal.ZERO);
			datos.agregarDato("senAJATR", Constantes.NO);
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
			datos.agregarDato("senWANG", new Integer(0));
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.NO);
			datos.agregarDato("senPosteo", senPosteo);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", new BigDecimal(1));
			datos.agregarDato("valorMovimiento", peticion.getValorCheques());
			datos.agregarDato("valorCheque", peticion.getValorCheques());
			datos.agregarDato("valorVenta", new BigDecimal(1));
			datos.agregarDato("numDocumentoTran2", new Integer(0));
			datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());
			datos.agregarDato("valorChequesExt", BigDecimal.ZERO);
			datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
			datos.agregarDato("descripcionTran", descripcionTran);
			//codBancoTransf -> null
			datos.agregarDato("numCuentaTransf", "0000000000000");
			//codPaisTransf -> null
			datos.agregarDato("senACRM", Constantes.SI);
			//codCliente ya esta por objeto cliente
			datos.agregarDato("valorImpuesto", datos.obtenerBigDecimal("saldoRealRecibo"));			
			datos.agregarDato("tipDocumentoCliente", peticion.getTipDocumentoPersona());
			datos.agregarDato("numDocumentoCliente", peticion.getNumDocumentoPersona());
			datos.agregarDato("numDocumentoImp", new Integer(0));
			datos.agregarDato("codSubCausal", new Integer(0));
			
			registrarTransaccionAAATR(datos);
			
		}
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		//10.2 Registrar evento del prestamo
		datos.agregarDato("cuentaPrestamo", peticion.getCuentaPrestamo());
		datos.agregarDato("fechaSistema", datos.obtenerInteger("fechaPago"));
		//horaSistema ya esta
		datos.agregarDato("codCausal", Constantes.PP_CAUSAL_PRESTAMO);//Ya se encuentra definido cuando se valida si es valor efectivo o cheque.
		datos.agregarDato("codPantalla", Constantes.ISPEC_PP210);
		//codCajero ya esta
		datos.agregarDato("codDestinoFondos", new Integer(0));
		datos.agregarDato("codOrigenFondos", new Integer(0));
		datos.agregarDato("codAccion", " ");
		//codMoneda ya esta objeto pcp
		datos.agregarDato("numResolucion", " ");
		datos.agregarDato("numActaResolucion", " ");
		datos.agregarDato("tasaAnualInteresAnticipado", new BigDecimal(0));
		//tasaAnualInteresCompensatorio ya esta objeto pcp
		datos.agregarDato("tasaAnualInteresMoratorio", pcp.getTasaAnualMora());
		datos.agregarDato("tasaAnualInteresCompensatorio", pcp.getTasaAnualInteresCompensatorio());
		datos.agregarDato("tasaAnualInteresVencido", pcp.getTasaAnualInteresNormal());
		datos.agregarDato("codUsuarioActual", " ");
		datos.agregarDato("codUsuarioAnterior", " ");
		datos.agregarDato("codModificacion", new Integer(0));
		datos.agregarDato("valorModificacion", " ");
		datos.agregarDato("senReversa", Constantes.NO);
		datos.agregarDato("fechaReversa", datos.obtenerInteger("fechaPago"));
		
		registrarEventosPrestamo(datos);
		datos.agregarDato("fechaSistema", fechaSistema);
		//10.3
		
		datos.agregarDato("cuentaRelacionada", new String("0"));
		datos.agregarDato("fechaSistemaAMD", datos.obtenerInteger("fechaPago"));
		datos.agregarDato("codCausal", Constantes.PP_CAUSAL_PRESTAMO);
		datos.agregarDato("codSubCausal", new Integer(0));
		datos.agregarDato("codConcepto", Constantes.CONCEPTO_PP);
		datos.agregarDato("codMonedaTran", new Integer(0));
		datos.agregarDato("codEstadoRegistro", pcp.getCodEstadoPrestamo());
		datos.agregarDato("fechaVencimientoImpago", pcp.getFechaVencimientoImpago());
		datos.agregarDato("saldoTerceroAntesTran", datos.obtenerValor("saldoTercerosValorAnterior"));
		datos.agregarDato("saldoTerceroDespuesTran", pcp.getSaldoTercerosDiaHoy());
		datos.agregarDato("tasaAnualInteresAnticipado", BigDecimal.ZERO);
		datos.agregarDato("tasaAnualInteresMoratorio", pcp.getTasaAnualMora());
		datos.agregarDato("tasaAnualInteresVencido", pcp.getTasaAnualInteresNormal());
		datos.agregarDato("valorInteresAnticipado", BigDecimal.ZERO);
		datos.agregarDato("valorGastosAjenosFacturados", datos.obtenerValor("valorGastoAjenoFacturadoNotaPP"));
		datos.agregarDato("valorGastosPropiosFacturados", datos.obtenerValor("valorGastoPropioFacturadoNotaPP"));
		datos.agregarDato("valorInteresCompensatorioFacturados", datos.obtenerValor("valorInteresCompensatorioFacturadoNotaPP"));
		datos.agregarDato("valorInteresVencidoAfectado", datos.obtenerValor("valorInteresVencidoAfectadoNotaPP"));
		datos.agregarDato("valorSeguroDanios", datos.obtenerValor("valorSeguroDaniosNotaPP"));
		datos.agregarDato("valorOtrosSeguros", BigDecimal.ZERO);
		datos.agregarDato("valorSeguroVida", datos.obtenerValor("valorSeguroVidaNotaPP"));
		datos.agregarDato("senReversa", Constantes.NO);
		datos.agregarDato("valorMovimiento",peticion.getValorMovimiento());
		registrarNotasPrestamo(datos);
		
		//10.4 registran tanque cuando banco prestamo es diferencte a BFA
		
		if (!UtileriaDeDatos.isEquals(pcp.getCodCompania(), Constantes.COD_COMPANIA_BFA)) {
			Integer codCausal = Constantes.CUENTA_POR_COBRAR_BANCARIA;
			datos.agregarDato("codCausalCtaCobrarBancaria", codCausal);
			datos.agregarDato("codCompania", pcp.getCodCompania());
			//Validando parametros de entrada para funcion de soporte registro transaccion AAATR.
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("codConcepto", codConcepto);
			datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("codDebCre", codDebCre);
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numDocumentoReversa", new Integer(0));
			datos.agregarDato("saldoAnterior", BigDecimal.ZERO);
			datos.agregarDato("senAJATR", Constantes.NO);
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			
			datos.agregarDato("senWANG", new Integer(0));
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.NO);
			datos.agregarDato("senPosteo", senPosteo);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", new BigDecimal(1));
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			datos.agregarDato("valorCheque", peticion.getValorCheques());
			datos.agregarDato("valorVenta", new BigDecimal(1));
			datos.agregarDato("numDocumentoTran2", new Integer(0));
			datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());
			datos.agregarDato("valorChequesExt", BigDecimal.ZERO);
			datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
			datos.agregarDato("descripcionTran", descripcionTran);
			datos.agregarDato("numCuentaTransf", "0000000000000");
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("valorImpuesto", datos.obtenerBigDecimal("saldoRealRecibo"));
			datos.agregarDato("tipDocumentoCliente", peticion.getTipDocumentoPersona());
			datos.agregarDato("numDocumentoCliente", peticion.getNumDocumentoPersona());
			datos.agregarDato("numDocumentoImp", new Integer(0));
			datos.agregarDato("codSubCausal", new Integer(0));
			
			registrarTransaccionAAATR(datos);
			
			codDebCre = Constantes.DEBITO;
			logger.debug(NOM_COD_SERVICIO + "Ejecutando setencia SELECT LINC SFBDB AAMCO, parametros: {}", pcp.getCodCompania());
			codCausal = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_AAMCO), Integer.class, pcp.getCodCompania());//TODO
			
			datos.agregarDato("codCausalCompania", codCausal);
			//Validando parametros de entrada para funcion de soporte registro transaccion AAATR.
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("codConcepto", codConcepto);
			datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
			datos.agregarDato("numTran", numTran);
			datos.agregarDato("codCompania", codCompa);
			datos.agregarDato("codDebCre", codDebCre);
			datos.agregarDato("fechaTran", datos.obtenerInteger("fechaSistema"));
			datos.agregarDato("numDocumentoReversa", new Integer(0));
			datos.agregarDato("saldoAnterior", BigDecimal.ZERO);
			datos.agregarDato("senAJATR", Constantes.NO);
			datos.agregarDato("senAutorizacion", Constantes.NO);
			datos.agregarDato("senReversa", Constantes.NO);
			//senSupervisor ya esta en peticion
			datos.agregarDato("senWANG", new Integer(0));
			datos.agregarDato("senDiaAnterior", Constantes.NO);
			datos.agregarDato("senImpCaja", Constantes.NO);
			datos.agregarDato("senPosteo", senPosteo);
			datos.agregarDato("valorAnterior", BigDecimal.ZERO);
			datos.agregarDato("valorCompra", new BigDecimal(1));
			datos.agregarDato("valorMovimiento", peticion.getValorMovimiento());
			datos.agregarDato("valorCheque", peticion.getValorCheques());
			datos.agregarDato("valorVenta", new BigDecimal(1));
			datos.agregarDato("numDocumentoTran2", new Integer(0));
			datos.agregarDato("valorChequesAjenos", peticion.getValorChequesAjenos());
			datos.agregarDato("valorChequesExt", BigDecimal.ZERO);
			datos.agregarDato("valorChequesPropios", peticion.getValorChequesPropios());
			datos.agregarDato("descripcionTran", descripcionTran);
			datos.agregarDato("numCuentaTransf", "0000000000000");
			datos.agregarDato("senACRM", Constantes.SI);
			datos.agregarDato("valorImpuesto", datos.obtenerBigDecimal("saldoRealRecibo"));
			datos.agregarDato("tipDocumentoCliente", peticion.getTipDocumentoPersona());
			datos.agregarDato("numDocumentoCliente", peticion.getNumDocumentoPersona());
			datos.agregarDato("numDocumentoImp", new Integer(0));
			datos.agregarDato("codSubCausal", new Integer(0));
			
			registrarTransaccionAAATR(datos);
			
		}
		
	}


	private void realizarPagoPrestamo(DatosOperacion datos) throws TipoDatoException, ServicioException, ParseException {
		//9.Realizar pago del prestamo
		logger.debug(NOM_COD_SERVICIO + "Realizando pago de prestamo...");
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		
		BigDecimal valorCapitalAfectado = BigDecimal.ZERO;
		BigDecimal valorInteresVencidoAfectadoNotaPP = BigDecimal.ZERO;
		BigDecimal valorInteresCompensatorioFacturadoNotaPP = BigDecimal.ZERO;
		BigDecimal valorGastoPropioFacturadoNotaPP = BigDecimal.ZERO;
		BigDecimal valorGastoAjenoFacturadoNotaPP = BigDecimal.ZERO;
		BigDecimal valorSeguroDaniosNotaPP = BigDecimal.ZERO;
		Integer	   fechaUltimoPago = new Integer(0);
		BigDecimal valorSeguroVidaNotaPP = BigDecimal.ZERO;
		BigDecimal saldoRealAnteriorRecibo = pcp.getSaldoReal();
		
		Integer fechaInicialAfectaAnticipo		= new Integer(0);
		Integer fechaFinalAfectaAnticipo		= new Integer(0);
		Integer fechaInicialAfectaVencido		= new Integer(0);
		Integer fechaFinalAfectaVencido			= new Integer(0);
		Integer numFacturaFinalAfectada			= new Integer(0);
		Integer numFacturaInicialAfectada		= new Integer(0);
		
		datos.agregarDato("fechaInicialAfectaAnticipo", fechaInicialAfectaAnticipo);
		datos.agregarDato("fechaFinalAfectaAnticipo", fechaFinalAfectaAnticipo);
		datos.agregarDato("fechaInicialAfectaVencido", fechaInicialAfectaVencido);
		datos.agregarDato("fechaFinalAfectaVencido", fechaFinalAfectaVencido);
		datos.agregarDato("numFacturaFinalAfectada", numFacturaFinalAfectada);
		datos.agregarDato("numFacturaInicialAfectada", numFacturaInicialAfectada);
		
		BigDecimal valorMoraAfectado			= BigDecimal.ZERO;
		BigDecimal saldoTercerosValorAnterior	= datos.obtenerBigDecimal("saldoTercerosValorAnterior");
		
		BigDecimal saldTer = pcp.getSaldoTercerosDiaHoy();
		String signoTercero1 = "-";
		if (!UtileriaDeDatos.isGreater(saldTer, BigDecimal.ZERO)) {
			signoTercero1 = "+";
		}
		if (UtileriaDeDatos.isEquals(saldTer, BigDecimal.ZERO)) {
			signoTercero1 = " ";
		}

		datos.agregarDato("signoTercero1", signoTercero1);
		
		logger.debug("==== OBJETO PCP: 		{}", pcp);
		logger.debug("==== SALDO TERCERO: 	{}", saldTer);
		BigDecimal saldoTercerosValorAnterior1	= pcp.getSaldoTercerosDiaHoy();
		pcp.setSaldoTercerosDiaHoy(pcp.getSaldoTercerosDiaHoy().add(peticion.getValorMovimiento()));

		if (UtileriaDeDatos.isGreater(pcp.getSaldoTercerosDiaHoy(), BigDecimal.ZERO)) {
			valorSeguroVidaNotaPP	= BigDecimal.ZERO;
			valorSeguroDaniosNotaPP = BigDecimal.ZERO;
			
			datos.agregarDato("saldoTercerosValorAnterior", saldoTercerosValorAnterior);
			RealizarPagoFacturaRespuesta resp = realizarPagoFactura(datos);
			
			fechaUltimoPago								= resp.getFechaUltimoPago();
			fechaInicialAfectaAnticipo					= resp.getFechaInicialAfectaAnticipo();
			fechaFinalAfectaAnticipo					= resp.getFechaFinalAfectaAnticipo();
			fechaInicialAfectaVencido					= resp.getFechaInicialAfectaVencido();
			fechaFinalAfectaVencido						= resp.getFechaFinalAfectaVencido();
			numFacturaFinalAfectada						= resp.getNumFacturaFinalAfectada();
			numFacturaInicialAfectada					= resp.getNumFacturaInicialAfectada(); 
			valorCapitalAfectado						= valorCapitalAfectado.add(resp.getValorCapitalAfectado());
			valorMoraAfectado							= valorMoraAfectado.add(resp.getValorMoraAfectado());
			valorGastoAjenoFacturadoNotaPP				= valorGastoAjenoFacturadoNotaPP.add(resp.getValorGastoAjenoFacturadoNotaPP());
			valorInteresCompensatorioFacturadoNotaPP	= valorInteresCompensatorioFacturadoNotaPP.add(resp.getValorInteresCompensatorioFacturadoNotaPP());
			valorGastoPropioFacturadoNotaPP				= valorGastoPropioFacturadoNotaPP.add(resp.getValorGastoPropioFacturadoNotaPP());
			valorInteresVencidoAfectadoNotaPP			= valorInteresVencidoAfectadoNotaPP.add(resp.getValorInteresVencidoAfectadoNotaPP());
			valorSeguroDaniosNotaPP						= resp.getValorSeguroDaniosNotaPP();
			valorSeguroVidaNotaPP						= resp.getValorSeguroVidaNotaPP();
			saldoTercerosValorAnterior					= pcp.getSaldoTercerosDiaHoy();

		}
		pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		BigDecimal interesPendientePago	= pcp.getSumInteresDevengadoNoFacturado();
		datos.agregarDato("interesPendientePago", interesPendientePago);
		Date fechaDesembolso				= UtileriaDeDatos.fecha8ToDateyyyyMMdd(pcp.getFechaDesembolso());
		Date fechaFinGraciaDate 			= UtileriaDeDatos.fechaSumDias(fechaDesembolso, pcp.getNumDiasGracia());
		Integer fechaFinGracia				= UtileriaDeDatos.tofecha8yyyyMMdd(fechaFinGraciaDate);
		Integer fechaPago 				= datos.obtenerInteger("fechaPago");

		if (
			!UtileriaDeDatos.isEquals(pcp.getCodTipoCuota(), Constantes.PP_PAGO_AL_VENCIMIENTO) && 
			 UtileriaDeDatos.isGreater(pcp.getSaldoTercerosDiaHoy(), interesPendientePago) && 
			 UtileriaDeDatos.isGreaterOrEquals(fechaPago, fechaFinGracia)
			) {
			
			//Parametro interesPendientePago ya se agrego al objeto datos(DatosOperacion)
			//Parametro pcp ya se agrego al objeto datos(DatosOperacion)
			logger.debug(NOM_COD_SERVICIO + "Invocando funcion interna Generar factura pago adelantado.");
			generarFacturaPagoAdelatado(datos);
			interesPendientePago = datos.obtenerBigDecimal("interesPendientePago");
			logger.debug(NOM_COD_SERVICIO + "Valor de interesPendientePago recuperado: {}", interesPendientePago);
			
		}
		
		BigDecimal valorCapitaAux = BigDecimal.ZERO;
		pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		if (
			UtileriaDeDatos.isGreater(pcp.getSaldoTeorico(), BigDecimal.ZERO) &&
			UtileriaDeDatos.isGreater(pcp.getSaldoTercerosDiaHoy(), BigDecimal.ZERO)
			) {
			
			if (UtileriaDeDatos.isGreaterOrEquals(interesPendientePago, pcp.getSaldoTercerosDiaHoy())) {
				interesPendientePago = pcp.getSaldoTercerosDiaHoy();
			}
			datos.agregarDato("interesPendientePago", interesPendientePago);
			BigDecimal saldosTercerosDiaHoyPcp = pcp.getSaldoTercerosDiaHoy();
			
			pcp.setSaldoTercerosDiaHoy(saldosTercerosDiaHoyPcp.subtract(interesPendientePago));
			valorCapitaAux = pcp.getSaldoTercerosDiaHoy();
			
			if (UtileriaDeDatos.isGreater(valorCapitaAux, pcp.getSaldoTeorico())) {
				valorCapitaAux = pcp.getSaldoTeorico();
			}
			
			
			//Agregando parametros de entrada para la funcion interna...
			datos.agregarDato("valorCapital", valorCapitaAux);
			FSAplicarAbonoExtra aplicarAbonoExtraFs = new FSAplicarAbonoExtra(getJdbcTemplate(), getDbLinkValue());
			aplicarAbonoExtraFs.aplicarAbonoExtra(datos);
			//Se omite el paso de asignar el valor retornado por la funcion ya que esta lo agrega el objeto datos
			//BigDecimal sumInteresDevengadoNoFacturadoAux = datos.obtenerBigDecimal("sumInteresDevengadoNoFacturadoAux");
		}
			pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
			BigDecimal valorCapitalFacturadoRecibo = datos.obtenerBigDecimal("valorCapitalFacturadoRecibo");
			BigDecimal valorInteresNormalFacturadoRecibo = datos.obtenerBigDecimal("valorInteresNormalFacturadoRecibo");
			BigDecimal valorOtrosGastosFacturadosRecibo = datos.obtenerBigDecimal("valorOtrosGastosFacturadosRecibo");
			BigDecimal valorTotalCobroRecibo = datos.obtenerBigDecimal("valorTotalCobroRecibo");
			
			String valorTotalCobroEnLetrasRecibo = "";
			BigDecimal valorTotalInteresAntesPago = datos.obtenerBigDecimal("valorTotalInteresAntesPago");
			if (UtileriaDeDatos.isGreater(pcp.getNumFactura(), pcp.getNumeroFacturaVigente())) {

				List<Map<String, Object>> recuperaDatosFactura = recuperaDatosFactura(datos);
				
				if (!UtileriaDeDatos.listIsEmptyOrNull(recuperaDatosFactura) && 
					UtileriaDeDatos.isNull(datos.obtenerValor("fechaVencimientoFactura"))) {
					AdaptadorDeMapa facAux = UtileriaDeDatos.adaptarMapa(recuperaDatosFactura.get(0));
					datos.agregarDato("fechaVencimientoFactura", facAux.getInteger("fechaVencimientoFactura"));
				}
				
				for (Map<String, Object> map : recuperaDatosFactura) {
					AdaptadorDeMapa factura = UtileriaDeDatos.adaptarMapa(map);
					
					valorCapitalFacturadoRecibo = valorCapitalFacturadoRecibo.add(factura.getBigDecimal("valorCapital"));
					valorInteresNormalFacturadoRecibo = valorInteresNormalFacturadoRecibo.add(factura.getBigDecimal("valorInteresNormalFacturado"));
					valorTotalInteresAntesPago		= valorTotalInteresAntesPago.add(factura.getBigDecimal("valorInteresNormalFacturado"));
					valorOtrosGastosFacturadosRecibo = valorOtrosGastosFacturadosRecibo
														.add(factura.getBigDecimal("valorGastoPropioFacturado"));
//														.add(factura.getBigDecimal("valorInteresNormalFacturado"));
					valorTotalCobroRecibo = valorTotalCobroRecibo.add(factura.getBigDecimal("valorCapital"))
																.add(factura.getBigDecimal("valorInteresNormalFacturado"))
																.add(factura.getBigDecimal("valorGastoPropioFacturado"))
																.add(factura.getBigDecimal("valorGastoAjenoPagado"));
					
				}
				datos.agregarDato("valorTotalInteresAntesPago", valorTotalInteresAntesPago);
				pcp.setSaldoTercerosDiaHoy(saldoTercerosValorAnterior);
				RealizarPagoFacturaRespuesta resp = realizarPagoFactura(datos);
				if (UtileriaDeDatos.isEquals(pcp.getSaldoTeorico(), new BigDecimal(0))) {
					pcp.setFechaProximaFacturacion(new Integer(99999999));
				}
				
				fechaUltimoPago								= resp.getFechaUltimoPago();
				fechaInicialAfectaAnticipo					= resp.getFechaInicialAfectaAnticipo();
				fechaFinalAfectaAnticipo					= resp.getFechaFinalAfectaAnticipo();
				fechaInicialAfectaVencido					= resp.getFechaInicialAfectaVencido();
				fechaFinalAfectaVencido						= resp.getFechaFinalAfectaVencido();
				numFacturaFinalAfectada						= resp.getNumFacturaFinalAfectada();
				numFacturaInicialAfectada					= resp.getNumFacturaInicialAfectada(); 
				valorCapitalAfectado						= valorCapitalAfectado.add(resp.getValorCapitalAfectado());
				valorMoraAfectado							= valorMoraAfectado.add(resp.getValorMoraAfectado());
				valorGastoAjenoFacturadoNotaPP				= valorGastoAjenoFacturadoNotaPP.add(resp.getValorGastoAjenoFacturadoNotaPP());
				valorInteresCompensatorioFacturadoNotaPP	= valorInteresCompensatorioFacturadoNotaPP.add(resp.getValorInteresCompensatorioFacturadoNotaPP());
				valorGastoPropioFacturadoNotaPP				= valorGastoPropioFacturadoNotaPP.add(resp.getValorGastoPropioFacturadoNotaPP());
				valorInteresVencidoAfectadoNotaPP			= valorInteresVencidoAfectadoNotaPP.add(resp.getValorInteresVencidoAfectadoNotaPP());
				valorSeguroDaniosNotaPP						= resp.getValorSeguroDaniosNotaPP();
				valorSeguroVidaNotaPP						= resp.getValorSeguroVidaNotaPP();
//				saldoTercerosValorAnterior					= resp.getSaldoTercerosValorAnterior();
				//19/03/2020
				//saldoTercerosValorAnterior					= pcp.getSaldoTercerosDiaHoy();
				
			}
			
			datos.agregarDato("fechaUltimoPago", fechaUltimoPago);
			
			BigDecimal valorTotalCapitalAbonadoRecibo				= BigDecimal.ZERO;
			BigDecimal valorInteresVencidoRecibo					= BigDecimal.ZERO;
			BigDecimal valorTotalInteresCompensatorioAbonadoRecibo	= BigDecimal.ZERO;
			BigDecimal valorInteresMoraRecibo						= BigDecimal.ZERO;
			BigDecimal valorOtrosGastosAbonadosRecibo				= BigDecimal.ZERO;
			BigDecimal valorPagoTotalInteres						= BigDecimal.ZERO;
			BigDecimal valorInteresDespuesPagoRecibo				= BigDecimal.ZERO;
			BigDecimal valorTotalAbonoRecibo						= BigDecimal.ZERO;
			BigDecimal valorCapitalDiferenciaRecibo					= BigDecimal.ZERO;
			BigDecimal valorInteresDiferenciaRecibo					= BigDecimal.ZERO;
			BigDecimal valorInteresCompensatorioDiferenciaRecibo	= BigDecimal.ZERO;
			BigDecimal valorMoraDiferenciaRecibo					= BigDecimal.ZERO;
			BigDecimal valorOtrosGastosDiferenciaRecibo				= BigDecimal.ZERO;
			BigDecimal valorTotalDiferenciaRecibo					= BigDecimal.ZERO;
			BigDecimal valorDiferenciaTercerosRecibo				= BigDecimal.ZERO;
			BigDecimal valorTercerosAbonoRecibo						= BigDecimal.ZERO;
			BigDecimal saldoRealRecibo								= BigDecimal.ZERO;
			BigDecimal saldoTercerosDiaHoyRecibo					= pcp.getSaldoTercerosDiaHoy();
			
			//Valores ya declarados en otros metodos
			BigDecimal valorInteresAntesPagoRecibo					= BigDecimal.ZERO;
			
			BigDecimal valorCompensatorioFacturado = datos.obtenerBigDecimal("valorCompensatorioFacturado");
			BigDecimal valorMoratorioFacturadoRecibo = datos.obtenerBigDecimal("valorMoratorioFacturadoRecibo");
			valorTotalInteresAntesPago = datos.obtenerBigDecimal("valorTotalInteresAntesPago");
			valorTotalCapitalAbonadoRecibo	= valorCapitalAfectado;
			valorInteresVencidoRecibo 		= valorInteresVencidoAfectadoNotaPP;
			valorTotalInteresCompensatorioAbonadoRecibo	= valorInteresCompensatorioFacturadoNotaPP;
			valorInteresMoraRecibo						= valorMoraAfectado;
			valorOtrosGastosAbonadosRecibo				= valorGastoPropioFacturadoNotaPP
															.add(valorGastoAjenoFacturadoNotaPP);
			valorInteresAntesPagoRecibo					= datos.obtenerBigDecimal("valorInteresAntesPagoRecibo");
			valorPagoTotalInteres						= valorTotalInteresCompensatorioAbonadoRecibo
															.add(valorInteresVencidoRecibo)
															.add(valorInteresMoraRecibo);
			                                                //.add(pcp.getSumInteresDevengadoNoFacturado());
			valorInteresDespuesPagoRecibo				= valorInteresAntesPagoRecibo
															.subtract(valorPagoTotalInteres);
			valorTotalAbonoRecibo						= valorInteresVencidoRecibo
															.add(valorTotalCapitalAbonadoRecibo)
															.add(valorTotalInteresCompensatorioAbonadoRecibo)
															.add(valorInteresMoraRecibo)
															.add(valorGastoPropioFacturadoNotaPP)
															.add(valorGastoAjenoFacturadoNotaPP)
															.subtract(saldoTercerosValorAnterior1);
			valorCapitalDiferenciaRecibo				= valorCapitalFacturadoRecibo.subtract(valorTotalCapitalAbonadoRecibo);
			valorInteresDiferenciaRecibo				= valorInteresNormalFacturadoRecibo.subtract(valorInteresVencidoRecibo);
			valorInteresCompensatorioDiferenciaRecibo	= valorCompensatorioFacturado.subtract(valorTotalInteresCompensatorioAbonadoRecibo);
			valorMoraDiferenciaRecibo					= valorMoratorioFacturadoRecibo.subtract(valorInteresMoraRecibo);
			valorOtrosGastosDiferenciaRecibo			= valorOtrosGastosFacturadosRecibo.subtract(valorOtrosGastosAbonadosRecibo);
			valorTotalDiferenciaRecibo					= valorTotalDiferenciaRecibo.add(valorTotalCobroRecibo);
			valorDiferenciaTercerosRecibo				= pcp.getSaldoTercerosDiaHoy();
			//20032020
			//valorTercerosAbonoRecibo					= pcp.getSaldoTercerosDiaHoy().subtract(saldoTercerosValorAnterior);
			valorTercerosAbonoRecibo					= saldoTercerosValorAnterior1;
			//
			valorTotalDiferenciaRecibo					= valorTotalDiferenciaRecibo.subtract(valorTotalAbonoRecibo);
			saldoRealRecibo								= pcp.getSaldoReal();
			
			FSValorEnLetras fsValorLetras = new FSValorEnLetras();
			calcularVariablesVencimiento(datos);
			valorTotalCobroEnLetrasRecibo = fsValorLetras.convertir(valorTotalAbonoRecibo.toString());
			
			BigDecimal saldoInteresNormalVencido = BigDecimal.ZERO;
			BigDecimal saldoCompensatorioVencido = BigDecimal.ZERO;
			BigDecimal saldoMoraVencido = BigDecimal.ZERO;
			Integer cantidadCuotasPagadas = new Integer(0);
			
			String codDestinoPcp			= StringUtils.leftPad(pcp.getCodDestino().toString(), 5, '0');
			String codEstadoPrestamoPcp		= String.valueOf(pcp.getCodEstadoPrestamo());
			
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT DESTINO PRESTAMO, parametros: {} y {}", "DESFON", codDestinoPcp);
			String destinoPrestamo		= jdbcTemplate.queryForObject(query(SELECT_DESTINO_PRESTAMO), String.class, "DESFON", codDestinoPcp);
			logger.debug(NOM_COD_SERVICIO + "Ejecutando setencia SELECT NOMBRE ESTADO PRESTAMO, parametros: {} y {}", "ESTPRESTAM", codEstadoPrestamoPcp);
			String nombreEstadoPrestamo	= jdbcTemplate.queryForObject(query(SELECT_NOMBRE_ESTADO_PRESTAMO), String.class, "ESTPRESTAM", codEstadoPrestamoPcp);
			datos.agregarDato("destinoPrestamo", destinoPrestamo);
			datos.agregarDato("nombreEstadoPrestamo", nombreEstadoPrestamo);
			
			Integer cantidadFacturasEmitidasNormalPcp = pcp.getCantidadFacturasEmitidasNormal();
			cantidadFacturasEmitidasNormalPcp = cantidadFacturasEmitidasNormalPcp + 1;
			
			if (UtileriaDeDatos.isGreater(cantidadFacturasEmitidasNormalPcp,99)) {
				cantidadFacturasEmitidasNormalPcp = 99;
			}
			
			pcp.setCantidadFacturasEmitidasNormal(cantidadFacturasEmitidasNormalPcp);
			
			saldoInteresNormalVencido	= datos.obtenerBigDecimal("saldoInteresNormalVencido");
			saldoMoraVencido			= datos.obtenerBigDecimal("saldoMoraVencido");
			saldoCompensatorioVencido	= datos.obtenerBigDecimal("saldoCompensatorioVencido");
			cantidadCuotasPagadas		= datos.obtenerInteger("cantidadCuotasPagadas");
			fechaUltimoPago				= datos.obtenerInteger("fechaUltimoPago");
			Integer dias = Math.abs(pcp.getCantDiasDesplazaVencHabiles());
			if (UtileriaDeDatos.isGreaterOrEquals(dias, new Integer(99))) {
				pcp.setCantDiasDesplazaVencHabiles(new Integer(99));  
			}
			if (UtileriaDeDatos.isGreaterOrEquals(cantidadCuotasPagadas, new Integer(999))) {
				cantidadCuotasPagadas=999;
			}
			if (UtileriaDeDatos.isGreaterOrEquals(pcp.getCantidadFacturasEmitidasNormal(), new Integer(99))) {
				pcp.setCantidadFacturasEmitidasNormal(new Integer(99));
			}
			
//			BigDecimal sumInteresDevengadoNoFacturadoAux = datos.obtenerBigDecimal("sumInteresDevengadoNoFacturadoAux");
			//TODO validar valor de propiedad sumInteresDevengadoNoFacturado
			Object[] paramsPPMPR = {
					pcp.getFechaProximaFacturacion(),
					pcp.getSumInteresDevengadoNoFacturado(), pcp.getSumCapitalPagadoHoy(),
					pcp.getSumCapitalPagadoMes(), pcp.getSumCapitalPagadoMesAnterior(),
					pcp.getSumCapitalReversadoMes(), pcp.getSumCapitalReversadoMesAnterior(),
					pcp.getInteresMoraPagadoHoy(), pcp.getInteresMoraPagado(),
					pcp.getSumInteresMoraReversadoMes(), pcp.getSumInteresMoraDevengado(),
					pcp.getSumAjusteDevengoInteresMora(), pcp.getInteresMoraCondonado(),
					pcp.getTotalInteresMoraCondonado(), pcp.getSumInteresCompensaPagadoHoy(),
					pcp.getSumInteresCompensaPagado(), pcp.getSumInteresCompensaReversadoMes(),
					pcp.getSumInteresCompensaReversMesAnt(), pcp.getSumInteresCompensaDevengado(),
					pcp.getSumAjusteDevengInteresCompensa(), pcp.getSumInteresCompensaCondonadoHoy(),
					pcp.getTotalInteresCompensaCondonado(), pcp.getFechaAcumulacionDevengo(),
					fechaUltimoPago, pcp.getNumFactura(),
					pcp.getSaldoTeorico(), pcp.getSaldoReal(),
					pcp.getSaldoVencido(), pcp.getSaldoTercerosDiaHoy(),
					pcp.getSaldoTercerosDiaSiguiente(), pcp.getCantidadFacturasEmitidasNormal(),
					cantidadCuotasPagadas, pcp.getFechaVencimientoImpago(),
					pcp.getCantDiasDesplazaVencHabiles(), pcp.getFechaProxFacturacionProrroga(),
					pcp.getSumMoraProrrogada(), pcp.getSaldoTotalMes(),
					fechaPago, pcp.getFechaVencimientoImpago(),
					pcp.getFechaVencimientoImpago(), saldoInteresNormalVencido,
					saldoMoraVencido, saldoCompensatorioVencido,
					pcp.getSumAjusteInteres(), pcp.getSumInteresDevengadoMes(),
					new Integer(1), pcp.getNumFacturaImpagada(), pcp.getSumInteresPagadoHoy(), 
					pcp.getSumInteresPagadoHastaUltimo(), pcp.getGlbDtime()
			};
			
			datos.agregarDato("pcp", pcp);
			datos.agregarDato("saldoTercerosDiaHoyRecibo", saldoTercerosDiaHoyRecibo);
			datos.agregarDato("valorTotalInteresCompensatorioAbonadoRecibo", valorTotalInteresCompensatorioAbonadoRecibo);
			datos.agregarDato("valorOtrosGastosAbonadosRecibo", valorOtrosGastosAbonadosRecibo);
			datos.agregarDato("valorInteresDespuesPagoRecibo", valorInteresDespuesPagoRecibo);
			datos.agregarDato("valorTotalAbonoRecibo", valorTotalAbonoRecibo);
			datos.agregarDato("valorCapitalDiferenciaRecibo", valorCapitalDiferenciaRecibo);
			datos.agregarDato("valorInteresDiferenciaRecibo", valorInteresDiferenciaRecibo);
			datos.agregarDato("valorInteresCompensatorioDiferenciaRecibo", valorInteresCompensatorioDiferenciaRecibo);
			datos.agregarDato("valorMoraDiferenciaRecibo", valorMoraDiferenciaRecibo);
			datos.agregarDato("valorOtrosGastosDiferenciaRecibo", valorOtrosGastosDiferenciaRecibo);
			datos.agregarDato("valorTotalDiferenciaRecibo", valorTotalDiferenciaRecibo);
			datos.agregarDato("valorDiferenciaTercerosRecibo", valorDiferenciaTercerosRecibo);
			datos.agregarDato("valorTercerosAbonoRecibo", valorTercerosAbonoRecibo);
			datos.agregarDato("valorTotalCobroEnLetrasRecibo", valorTotalCobroEnLetrasRecibo);
			datos.agregarDato("saldoRealRecibo", saldoRealRecibo);
			datos.agregarDato("saldoRealAnteriorRecibo", saldoRealAnteriorRecibo);
			datos.agregarDato("valorInteresVencidoRecibo", valorInteresVencidoRecibo);
			datos.agregarDato("valorInteresMoraRecibo", valorInteresMoraRecibo);
			datos.agregarDato("valorTotalCapitalAbonadoRecibo", valorTotalCapitalAbonadoRecibo);
			
			datos.agregarDato("saldoTercerosValorAnterior", saldoTercerosValorAnterior1);
			datos.agregarDato("valorCapitalAfectado", valorCapitalAfectado);
			datos.agregarDato("valorMoraAfectado", valorMoraAfectado);
			datos.agregarDato("valorGastoAjenoFacturadoNotaPP", valorGastoAjenoFacturadoNotaPP);
			datos.agregarDato("valorGastoPropioFacturadoNotaPP", valorGastoPropioFacturadoNotaPP);
			datos.agregarDato("valorInteresCompensatorioFacturadoNotaPP", valorInteresCompensatorioFacturadoNotaPP);
			datos.agregarDato("valorInteresVencidoAfectadoNotaPP", valorInteresVencidoAfectadoNotaPP);
			datos.agregarDato("valorSeguroDaniosNotaPP", valorSeguroDaniosNotaPP);
			datos.agregarDato("valorSeguroVidaNotaPP", valorSeguroVidaNotaPP);
			
			datos.agregarDato("valorCapitalFacturadoRecibo", valorCapitalFacturadoRecibo);
			datos.agregarDato("valorInteresNormalFacturadoRecibo", valorInteresNormalFacturadoRecibo);
			datos.agregarDato("valorOtrosGastosFacturadosRecibo", valorOtrosGastosFacturadosRecibo);
			datos.agregarDato("valorTotalCobroRecibo", valorTotalCobroRecibo);
			
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia UPDATE LINC SFBDB PPMPR, parametros: {}", Arrays.toString(paramsPPMPR));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_PPMPR), paramsPPMPR);
		
	}
	
	private List<Map<String, Object>> recuperaDatosFactura(DatosOperacion datos) throws TipoDatoException{
		
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		Object[] paramsPPMFA = {
			pcp.getCodOficina(),
			pcp.getCodProducto(),
			pcp.getNumCuenta(),
			pcp.getNumeroFacturaVigente(),
			new Integer(0),
			Constantes.PP_ESTADO_FACTURA_IMPAGA,
			Constantes.PP_ESTADO_FACTURA_PARCIAL
		};
		
		logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB PPMFA 1, parametros: {}", Arrays.toString(paramsPPMFA));
		List<Map<String, Object>> queryForList = this.jdbcTemplate.queryForList(query(SELECT_LINC_SFBDB_PPMFA_1), paramsPPMFA);
		
		return queryForList;
	}

	private void validacionMontos(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		
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
	
	private void calcularValoresRecibo(DatosOperacion datos) throws TipoDatoException, ServicioException, ParseException {

		//Paso 7. Inicializando valor de saldos de terceros.
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		
		BigDecimal valorTotalCobroRecibo			= BigDecimal.ZERO;
		BigDecimal saldoTercerosValorAnterior		= pcp.getSaldoTercerosDiaHoy();
		BigDecimal saldoTercerosValorAnteriorRecibo	= pcp.getSaldoTercerosDiaHoy();
		BigDecimal InteresPendienteNoFacturado =  pcp.getSumInteresDevengadoNoFacturado();
		
		//prepararInformacionDevengo(datos);
		//calcularDiferenciaDevengo(datos);

		
		datos.agregarDato("saldoTercerosValorAnterior", saldoTercerosValorAnterior);
		datos.agregarDato("saldoTercerosValorAnteriorRecibo", saldoTercerosValorAnteriorRecibo);
		
		//Paso 8. Calcular valores a imprimir.
		Integer contadorFacturas						= new Integer(0);
		BigDecimal valorCapitalFacturadoRecibo			=  BigDecimal.ZERO;
		BigDecimal valorInteresNormalFacturadoRecibo	=  BigDecimal.ZERO;
		BigDecimal valorCompensatorioFacturado			=  BigDecimal.ZERO;
		BigDecimal valorMoratorioFacturadoRecibo		=  BigDecimal.ZERO;
		BigDecimal valorOtrosGastosFacturadosRecibo		=  BigDecimal.ZERO;
		BigDecimal valorInteresAntesPagoRecibo			=  BigDecimal.ZERO;
		BigDecimal valorInteresCompensatorioPendiente	=  BigDecimal.ZERO;
		BigDecimal valorInteresMoraPendiente			=  BigDecimal.ZERO;
		
		Object[] paramsPPMFA = {
			pcp.getCodOficina(),
			pcp.getCodProducto(),
			pcp.getNumCuenta(),
			Constantes.PP_ESTADO_FACTURA_IMPAGA,
			Constantes.PP_ESTADO_FACTURA_PARCIAL
		};
		
		logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB PPMFA, parametros: " + Arrays.toString(paramsPPMFA));
		List<Map<String, Object>> facturasPendientes = jdbcTemplate.queryForList(query(SELECT_LINC_SFBDB_PPMFA), paramsPPMFA);

		if (!UtileriaDeDatos.listIsEmptyOrNull(facturasPendientes)) {
			AdaptadorDeMapa facAux = UtileriaDeDatos.adaptarMapa(facturasPendientes.get(0));
			datos.agregarDato("fechaVencimientoFactura", facAux.getInteger("fechaVencimientoFactura"));
		}
		
		for (Map<String, Object> facturaMap : facturasPendientes) {
			AdaptadorDeMapa factura = UtileriaDeDatos.adaptarMapa(facturaMap);
			
			contadorFacturas += 1;
			valorTotalCobroRecibo 		= valorTotalCobroRecibo.add(factura.getBigDecimal("valorCapital"))
												.subtract(factura.getBigDecimal("valorCapitalPagado"));
			
			valorCapitalFacturadoRecibo = valorCapitalFacturadoRecibo.add(factura.getBigDecimal("valorCapital"))
											.subtract(factura.getBigDecimal("valorCapitalPagado"));
			
			valorTotalCobroRecibo		= valorTotalCobroRecibo.add(factura.getBigDecimal("valorInteresNormalFacturado"))
											.subtract(factura.getBigDecimal("valorInteresNormalPagado"));
			
			valorInteresAntesPagoRecibo = valorInteresAntesPagoRecibo
											.add(factura.getBigDecimal("valorInteresNormalFacturado"))
											.subtract(factura.getBigDecimal("valorInteresNormalPagado"))
											.add(factura.getBigDecimal("valorInteresCompenFacturado"))
											.subtract(factura.getBigDecimal("valorInteresCompenPagado"))
											.add(factura.getBigDecimal("valorInteresMoraFacturado"))
											.subtract(factura.getBigDecimal("valorInteresMoraPagado"))
											;
			
			valorInteresNormalFacturadoRecibo = valorInteresNormalFacturadoRecibo
												.add(factura.getBigDecimal("valorInteresNormalFacturado"))
												.subtract(factura.getBigDecimal("valorInteresNormalPagado"))
												;
			
			valorInteresCompensatorioPendiente = factura.getBigDecimal("valorInteresCompenFacturado")
												.subtract(factura.getBigDecimal("valorInteresCompenPagado"))
												;
			
			valorInteresMoraPendiente = factura.getBigDecimal("valorInteresMoraFacturado")
										.subtract(factura.getBigDecimal("valorInteresMoraPagado"))
										;
			
			if (
				UtileriaDeDatos.isGreater(factura.getBigDecimal("valorGastoAjenoFacturado"), BigDecimal.ZERO) || 
				UtileriaDeDatos.isGreater(factura.getBigDecimal("valorGastoPropioFacturado"), BigDecimal.ZERO)
					) {
				valorOtrosGastosFacturadosRecibo = valorOtrosGastosFacturadosRecibo
													.add(factura.getBigDecimal("valorGastoPropioFacturado"))
													.subtract(factura.getBigDecimal("valorGastoPropioPagado"))
													.add(factura.getBigDecimal("valorGastoAjenoFacturado"))
													.subtract(factura.getBigDecimal("valorGastoAjenoPagado"))
													;
				
				valorTotalCobroRecibo = valorTotalCobroRecibo
										.add(factura.getBigDecimal("valorGastoPropioFacturado"))
										.subtract(factura.getBigDecimal("valorGastoPropioPagado"))
										.add(factura.getBigDecimal("valorGastoAjenoFacturado"))
										.subtract(factura.getBigDecimal("valorGastoAjenoPagado"))
										;
			
			}
			
			valorCompensatorioFacturado = valorCompensatorioFacturado
											.add(valorInteresCompensatorioPendiente)
											;
			
			valorMoratorioFacturadoRecibo = valorMoratorioFacturadoRecibo
											.add(valorInteresMoraPendiente)
											;
			
			valorTotalCobroRecibo = valorTotalCobroRecibo
									.add(valorInteresCompensatorioPendiente)
									.add(valorInteresMoraPendiente)
									;
			
		}
		
		valorTotalCobroRecibo = valorTotalCobroRecibo.subtract(pcp.getSaldoTercerosDiaHoy());
		
		valorInteresAntesPagoRecibo = valorInteresAntesPagoRecibo.add(InteresPendienteNoFacturado);
		
		BigDecimal valorTotalInteresAntesPago = valorInteresNormalFacturadoRecibo
												.add(valorCompensatorioFacturado)
												.add(valorMoratorioFacturadoRecibo)
												.add(InteresPendienteNoFacturado);
		                                        
		
		datos.agregarDato("valorTotalInteresAntesPago", valorTotalInteresAntesPago);
		datos.agregarDato("valorTotalCobroRecibo", valorTotalCobroRecibo);
		datos.agregarDato("saldoTercerosValorAnterior", saldoTercerosValorAnterior);
		datos.agregarDato("saldoTercerosValorAnteriorRecibo", saldoTercerosValorAnteriorRecibo);
		datos.agregarDato("contadorFacturas", contadorFacturas);
		datos.agregarDato("valorCapitalFacturadoRecibo", valorCapitalFacturadoRecibo);
		datos.agregarDato("valorInteresNormalFacturadoRecibo", valorInteresNormalFacturadoRecibo);
		datos.agregarDato("valorCompensatorioFacturado", valorCompensatorioFacturado);
		datos.agregarDato("valorMoratorioFacturadoRecibo", valorMoratorioFacturadoRecibo);
		datos.agregarDato("valorOtrosGastosFacturadosRecibo", valorOtrosGastosFacturadosRecibo);
		datos.agregarDato("valorInteresAntesPagoRecibo", valorInteresAntesPagoRecibo);
		datos.agregarDato("valorInteresCompensatorioPendiente", valorInteresCompensatorioPendiente);
		datos.agregarDato("valorInteresMoraPendiente", valorInteresMoraPendiente);
		
	}
	
	private void validarPagoNoExcedeDeuda(DatosOperacion datos) throws TipoDatoException, ParseException, ServicioException {
		
		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		Integer fechaSistema = datos.obtenerInteger("fechaSistema");
		Date fechaSistemaD = UtileriaDeDatos.fecha6ToDate(fechaSistema);
		Integer fechaPago = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistemaD);
		datos.agregarDato("fechaPago", fechaPago);
		
		Object[] paramsMontoDeuda = {
			pcp.getCodProducto(),
			pcp.getCodOficina(),
			pcp.getNumCuenta(),
			Constantes.PP_ESTADO_FACTURA_IMPAGA,
			Constantes.PP_ESTADO_FACTURA_PARCIAL,
			fechaPago,
			pcp.getCodProducto(),
			pcp.getCodOficina(),
			pcp.getNumCuenta(),
			pcp.getCodProducto(),
			pcp.getCodOficina(),
			pcp.getNumCuenta()
		};
		
		
		
		
		BigDecimal montoDeuda = jdbcTemplate.queryForObject(query(SELECT_MONTO_DEUDA), BigDecimal.class, paramsMontoDeuda);
		//prepararInformacionDevengo(datos);
		//pagoMora(datos);
		//montoDeuda.add(datos.obtenerBigDecimal("valorRubroCalculado"));
		
		//pagoInteresCompensatorio(datos);
		//montoDeuda.add(datos.obtenerBigDecimal("valorRubroCalculado"));
		BigDecimal valMovSalTer = peticion.getValorMovimiento().add(pcp.getSaldoTercerosDiaHoy());
		if (UtileriaDeDatos.isGreater(valMovSalTer, montoDeuda)) {
			throw new ServicioException(21160, "Monto de pago es mayor al adeudado.");
		}
		
	}

	private void pagoMora(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		// Cacular el interes mora no facturada
		        CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
				Integer tipoTasaInteres = Constantes.PP_INTERES_MORA;
				BigDecimal valorInteresMoratorioFacturado    		= BigDecimal.ZERO;
				
				datos.agregarDato("codBaseCobroInteres", pcp.getCodBaseCalculoMora());
				datos.agregarDato("tipoTasaInteres", tipoTasaInteres);
				datos.agregarDato("tasaInteres", pcp.getTasaInteresMora());
				
		/*		FSCalcularDevengo fs = new FSCalcularDevengo(getJdbcTemplate(), getDbLinkValue());
				fs.calcularDevengo(datos);
				BigDecimal montoDevengado = datos.obtenerBigDecimal("montoDevengado");				
				BigDecimal montoAjustado = datos.obtenerBigDecimal("montoAjustado");
				datos.agregarDato("valorRubroCalculadoIntermedio", BigDecimal.ZERO);
				datos.agregarDato("montoDevengado", montoDevengado);
				datos.agregarDato("montoAjustado", montoAjustado);
				datos.agregarDato("valorRubroCalculado", BigDecimal.ZERO);
				FSCalcularDevengoRubroPrestamo fsRubro = new FSCalcularDevengoRubroPrestamo(getJdbcTemplate(), getDbLinkValue());				
			
				fsRubro.calcularDevengoRubroPrestamo(datos);				
			*/	
				
				//
		
	}

	private void pagoInteresCompensatorio(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		// Cacular el interes compesatorio no facturada
		        CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		        Integer tipoTasaInteres = Constantes.PP_INTERES_VENCIDO;
		        
				datos.agregarDato("codBaseCobroInteres", pcp.getCodBaseCobroCompensatorio());
				datos.agregarDato("tipoTasaInteres", tipoTasaInteres);
				datos.agregarDato("tasaInteres", pcp.getTasaInteresCompensatorio());

				FSCalcularDevengo fs = new FSCalcularDevengo(getJdbcTemplate(), getDbLinkValue());
				fs.calcularDevengo(datos);

				FSCalcularDevengoRubroPrestamo fsRubro = new FSCalcularDevengoRubroPrestamo(getJdbcTemplate(), getDbLinkValue());
				
				fsRubro.calcularDevengoRubroPrestamo(datos);
				
				
				//
		
	}
	
	private void validacionDeTransaccion(DatosOperacion datos) throws TipoDatoException, ParseException, ServicioException {
		
		CuentaPrestamo pcp = datos.obtenerObjeto("pcp", CuentaPrestamo.class);
		PagoPrestamoPeticion peticion = datos.obtenerObjeto("peticion", PagoPrestamoPeticion.class);
		
		BigDecimal sumInteresDevengadoNoFacturadoAux = pcp.getSumInteresDevengadoNoFacturado();
		//Guardando valor para ser utilizando en los siguientes pasos
		datos.agregarDato("sumInteresDevengadoNoFacturadoAux", sumInteresDevengadoNoFacturadoAux);
		
		Integer senPosteo = Constantes.NO;
		
		String nomCompania = jdbcTemplate.queryForObject(query(SELECT_NOM_COMPANIA), String.class, pcp.getCodCompania());
		datos.agregarDato("nomCompania", nomCompania);
		//Guardando valor para ser utilizando en los siguientes pasos
		datos.agregarDato("senPosteo", senPosteo);
		datos.agregarDato("codEstadoPrestamo", pcp.getCodEstadoPrestamo());
		datos.agregarDato("codBloqueo", pcp.getCodBloqueo());
		//validarEstadoPrestamos(datos);
		
		Object[] paramsPPRGP = {
			pcp.getCodOficina(),
			pcp.getCodProducto(),
			pcp.getNumCuenta(),
			new Integer(91),
			new Integer(92),
			new Integer(94),
			new Integer(0),
			Constantes.PP_ESTADO_GASTO_VIGENTE
		};
		
		logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB PPRGP, parametros: " + Arrays.toString(paramsPPRGP));
		BigDecimal cantidadGastos = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_PPRGP), BigDecimal.class, paramsPPRGP);
		
		if (UtileriaDeDatos.isGreater(cantidadGastos, BigDecimal.ZERO)) {
			logger.error(NOM_COD_SERVICIO + "Transaccion requiere pago de gastos por seguro, cantidadGastos: {}", cantidadGastos);
			throw new ServicioException(20890, "Transaccion requiere pago de gastos por seguro.");
		}
		
		Object[] paramsPPRGP1 = {
				pcp.getCodOficina(),
				pcp.getCodProducto(),
				pcp.getNumCuenta(),
				Constantes.PP_ESTADO_GASTO_VIGENTE
			};
			
			logger.debug(NOM_COD_SERVICIO + "Ejecutando sentencia SELECT LINC SFBDB PPRGP1, parametros: " + Arrays.toString(paramsPPRGP1));
			BigDecimal sumGastosCovid = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_PPRGP1), BigDecimal.class, paramsPPRGP1);
			if (UtileriaDeDatos.isNull(sumGastosCovid)) {
				 sumGastosCovid = BigDecimal.ZERO;
			} 
		    datos.agregarDato("sumGastosCovid", sumGastosCovid);
		
		Object[] paramsAAATR = {
			datos.obtenerInteger("fechaRelativa"),
			peticion.getCodOficinaTran(),
			peticion.getCodTerminal(),
			new Integer(0),
			peticion.getCodTran(),
			pcp.getCodProducto(),
			pcp.getCodOficina(),
			pcp.getNumCuenta(),
			pcp.getDigitoVerificador(),
			peticion.getNumDocumentoTran()
		//	peticion.getValorMovimiento(),
		//	Constantes.SI
		};
		
		BigDecimal cantidadTransaccion = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_AAATR), BigDecimal.class, paramsAAATR);
		
		if (UtileriaDeDatos.isGreater(cantidadTransaccion, BigDecimal.ZERO)) {
			logger.error(NOM_COD_SERVICIO + "Ya existe pago exactamente igual.");
			throw new ServicioException(20020, "Ya existe - {}", "PAGO EXACTAMENTE IGUAL") ;
		}
		
		BigDecimal valorEfectivo	= peticion.getValorEfectivo();
		BigDecimal valorCheques		= peticion.getValorCheques();
		BigDecimal valorMovimiento	= peticion.getValorMovimiento();
		
		BigDecimal valorTotal = valorEfectivo.add(valorCheques);
		
		if (!UtileriaDeDatos.isEquals(valorMovimiento, valorTotal)) {
			logger.error(NOM_COD_SERVICIO + "Valor incorrecto, el total debe ser igual efectivo + valores. ValorTotal: {} ValorMovimiento: {}", 
												valorTotal, valorMovimiento);
			throw new ServicioException(20018, "Valor incorrecto, el total debe ser igual efectivo + valores.",
											"EL TOTAL DEBE SER IGUAL A EFECTIVO + VALORES");
		}
		
	}


	/**
	 * Validaciones iniciales sobre parametros recibidos como parte de la peticion
	 * @param peticion
	 * @throws ServicioException
	 */
	private void validacionInicial(PagoPrestamoPeticion peticion) throws ServicioException {
		
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCuentaPrestamo(), "cuentaPrestamo", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaPrestamo(), "cuentaPrestamo", TipoValidacion.CADENA_NUMERICA);
		UtileriaDeParametros.validarParametro(peticion.getCuentaPrestamo(), "cuentaPrestamo", TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
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
		
		if (
			!UtileriaDeDatos.listIsEmptyOrNull(peticion.getCheques())
				) {
			for (Cheque cheque : peticion.getCheques()) {
				
				Integer numCheque = cheque.getNumCheque();
				UtileriaDeParametros.validarParametro(cheque.getNumCheque(), "numCheque del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
				UtileriaDeParametros.validarParametro(cheque.getTipCheque(), "tipCheque del cheque: " + numCheque, TipoValidacion.ENTERO_VALOR_EN, new Integer[] {1,3,5});
			
				if (
					UtileriaDeDatos.in(cheque.getTipCheque(), 1, 2) 
					) {
					UtileriaDeParametros.validarParametro(cheque.getCodPantalla(), "codPantalla del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(cheque.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(cheque.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_NUMERICA);
					UtileriaDeParametros.validarParametro(cheque.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.LONGITUD_CADENA, new Integer[] {13});
					UtileriaDeParametros.validarParametro(cheque.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(cheque.getNumAutorizacion(), "numAutorizacion del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
					UtileriaDeParametros.validarParametro(cheque.getCodCausal(), "codCausal del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(cheque.getCodTran(), "codTran del cheque: " + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
				}
				
				if (
					UtileriaDeDatos.isEquals(cheque.getTipCheque(), 3)
					) {
					UtileriaDeParametros.validarParametro(cheque.getCuentaCheque(), "cuentaCheque del cheque: " + numCheque, TipoValidacion.CADENA_VACIA);
					UtileriaDeParametros.validarParametro(cheque.getCodBancoCheque(), "codBancoCheque del cheque: " + numCheque, TipoValidacion.ENTERO_DIFERENTE_CERO);
					UtileriaDeParametros.validarParametro(cheque.getValorCheque(), "valorCheque del cheque: " + numCheque, TipoValidacion.BIGDECIMAL_MAYOR_CERO);
					UtileriaDeParametros.validarParametro(cheque.getCodPlazaCheque(), "codPlazaCheque del cheque" + numCheque, TipoValidacion.ENTERO_MAYOR_CERO);
					}
				
			}
		}
		
	}

}
