package sv.gob.bfa.pagaduriagenerico.servicio;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.model.CuentaAhorro;
import sv.gob.bfa.core.model.CuentaCorriente;
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
import sv.gob.bfa.pagaduriagenerico.model.PagaduriaGenericoPeticion;
import sv.gob.bfa.pagaduriagenerico.model.PagaduriaGenericoRespuesta;

public class PagaduriaGenericoServicio extends Servicio{
	
	private static final String NOM_COD_SERVICIO = "Pagaduria Generico AJ599: ";
	
	private static final String SELECT_SFBDB_IEMCO = 
			"SELECT IVA_MORA AS valorMora," + 
			"		IPO_MORA AS porcentajeMora," + 
			"		IDE_ALIAS AS aliasIEMCO," + 
			"		SCO_IDENT AS codClienteIEMCO" + 
			"	FROM LINC.SFBDB_IEMCO@DBLINK@" + 
			"	WHERE ICO_COLECT = ?";
	
	private static final String SELECT_SFBDB_ICRDO = 
			"SELECT ACO_CAUSA AS codCausal," + 
			"		ACOSUBCAU AS codSubCausal," + 
			"		ISEVALREG AS senValidacionRegistro," + 
			"		ISEABOCTA AS senAbonoCuenta, " + 
			"		ACOTIPCOR AS codTipoCorrelativo," + 
			"		ISEPAGEXT AS senPagoExtemporaneo," + 
			"		IDEDOCUM AS descTipoDocumento" + 
			"	FROM LINC.SFBDB_ICRDO@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?";
	
	private static final String SELECT_SFBDB_AAMTM = 
			"SELECT ASESUBCAU AS senSubCausal" + 
			"	FROM LINC.SFBDB_AAMTM@DBLINK@" + 
			"	WHERE	DCO_ISPEC = ?" + 
			"		AND ACO_CAUSA = ?";

	private static final String SELECT_SFBDB_AAMSM = 
			"SELECT ACU_PRODU AS codProductoCta," + 
			"		ACU_OFICI AS codOficinaCta, " + 
			"		ACUNUMCUE AS numCuenta," + 
			"		ACUDIGVER AS digitoVerificadorCta," + 
			" 		LPAD(ACU_PRODU, 3, 0) || LPAD(ACU_OFICI, 3, 0) || LPAD(ACUNUMCUE, 6, 0) || ACUDIGVER AS cuentaStr" + 
			"	FROM LINC.SFBDB_AAMSM@DBLINK@" + 
			"	WHERE	DCO_ISPEC = ?" + 
			"		AND ACO_CAUSA = ?" + 
			"		AND ACOSUBCAU = ?";
	
	private static final String SELECT_SFBDB_IEAGE = 
			"SELECT INO_CAMPO AS nombreCampo," + 
			"		ISEVISIBLE AS senVisible," + 
			"		INU_LARGO AS longitudCampo," + 
			"		ISE_OBLIGA AS senObligatorio," + 
			"		ISE_ARCHI AS senArchivo" + 
			"	FROM LINC.SFBDB_IEAGE@DBLINK@" + 
			"	WHERE	ICO_COLECT = ?" + 
			"		AND INU_CAMPO > ?" + 
			"		ORDER BY INU_CAMPO";
	
	private static final String SELECT_SFBDB_ICPGE = 
			"SELECT INO_CLIEN AS nomCliente, " + 
			"		INUREGCLI AS numRegistroCliente, " + 
			"		ICO_ESTAD AS codEstado," + 
			"		IFE_PAGO AS fechaPago,  " + 
			"		IVA_PAGO AS valorPago, " + 
			"		ICOTIPCLI AS tipoClienteICPGE," + 
			"		IFE_CARGA AS fechaCarga, " + 
			"		IHO_CARGA AS horaCarga," + 
			"		IFE_VENCI AS fechaVencimiento," + 
			"		INU_DOCUM AS numDocumento," + 
			"		SUBSTR(INUIDECLI, 1,8) AS convenio" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?" + 
			"		AND INUREGCLI = ?" + 
			"		AND INUIDECLI = ?" +
			"		AND GLB_DTIME = ?" + 
			"       AND ROWNUM <= 1" + 
			"		ORDER BY GLB_DTIME ASC"
			; 
	
	private static final String SELECT_SFBDB_ICPGE2 = 
			"SELECT INO_CLIEN AS nomCliente," + 
			"		INUREGCLI AS numRegistroCliente," + 
			"		ICO_ESTAD AS codEstado," + 
			"		IFE_PAGO AS fechaPago," + 
			"		IVA_PAGO AS valorPago," + 
			"		ICOTIPCLI AS tipoClienteICPGE," + 
			"		IFE_CARGA AS fechaCarga," + 
			"		IHO_CARGA AS horaCarga," + 
			"		IFE_VENCI AS fechaVencimiento," + 
			"		INU_DOCUM AS numDocumento," + 
			"		SUBSTR(INUIDECLI, 1,8) AS convenio" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?" + 
			"		AND INUREGCLI = ?" + 
			"		AND INUIDECLI = ?" + 
			"		AND IFE_PAGO = ?"  + 
			"		AND GLB_DTIME = ?" + 
			"       AND ROWNUM <= 1" + 
			"		ORDER BY GLB_DTIME ASC";
	
	private static final String SELECT_SFBDB_ICPGE3 = 
			"SELECT INO_CLIEN AS nomCliente, " + 
			"		INUREGCLI AS numRegistroCliente, " + 
			"		ICO_ESTAD AS codEstado," + 
			"		IFE_PAGO AS fechaPago,  " + 
			"		IVA_PAGO AS valorPago, " + 
			"		ICOTIPCLI AS tipoClienteICPGE," + 
			"		IFE_CARGA AS fechaCarga, " + 
			"		IHO_CARGA AS horaCarga," + 
			"		IFE_VENCI AS fechaVencimiento," + 
			"		INU_DOCUM AS numDocumento," + 
			"		SUBSTR(INUIDECLI, 1,8) AS convenio" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?" + 
			"		AND INUREGCLI = ?" + 
			"		AND GLB_DTIME = ?" + 
			"       AND ROWNUM <= 1" + 
			"		ORDER BY ICO_COLECT, ICOTIPDOC, INUREGCLI DESC";
	
	private static final String SELECT_SFBDB_ICPGE4 = 
			"SELECT INO_CLIEN AS nomCliente, " + 
			"		INUREGCLI AS numRegistroCliente, " + 
			"		ICO_ESTAD AS codEstado," + 
			"		IFE_PAGO AS fechaPago,  " + 
			"		IVA_PAGO AS valorPago, " + 
			"		ICOTIPCLI AS tipoClienteICPGE," + 
			"		IFE_CARGA AS fechaCarga, " + 
			"		IHO_CARGA AS horaCarga," + 
			"		IFE_VENCI AS fechaVencimiento," + 
			"		INU_DOCUM AS numDocumento," + 
			"		SUBSTR(INUIDECLI, 1,8) AS convenio" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?" + 
			"		AND INUREGCLI = ?"  + 
			"		AND GLB_DTIME = ?" + 
			"       AND ROWNUM <= 1" + 
			"		ORDER BY GLB_DTIME ASC";
	
	private static final String UPDATE_SFBDB_ICPGE = 
			"UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET ICO_ESTAD = ?" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?" + 
			"   	AND INUREGCLI = ?" + 
			"   	AND INUIDECLI = ?" +
			"       AND IFE_CARGA = ?" + 
			"		AND GLB_DTIME = ?"
			;
	
	private static final String SELECT_SFBDB_IEAPC = 
			"SELECT IFE_VENCIM AS fechaVencimiento" + 
			"	FROM (SELECT IFE_VENCIM" + 
			"			FROM LINC.SFBDB_IEAPC@DBLINK@" + 
			"			WHERE	ICO_COLECT = ?" + 
			"				AND ICO_TIPDOC = ?" + 
			"				AND IFE_VENCIM >= ?" + 
			"			ORDER BY IFE_VENCIM DESC" + 
			"	) WHERE ROWNUM <= ?";
	
	private static final String SELECT_SFBDB_ICATR = 
			"SELECT INUREGCLI AS numRegistroCliente," + 
			"		TNUDOCTRA AS numDocumentoTran," + 
			"		IMO_PAGO AS montoPago" + 
			"   FROM LINC.SFBDB_ICATR@DBLINK@" + 
			"   WHERE	IFE_TRANS = ?" + 
			"   	AND DCO_OFICI = ?" + 
			"   	AND DCO_TERMI = ?" + 
			"   	AND DCO_USUAR = ?" + 
			"		AND INUREGCLI = ?" +
			"   	AND TNUDOCTRA = ?" + 
			"   	AND IMO_PAGO = ?" + 
			"   	AND INU_REGIS > ? " + 
			"   	AND TSE_REVER != ?";
	
	private static final String SELECT_SFBDB_AAACM = 
			"SELECT ADEREGCAR AS numRegArchivoCargado" + 
			"   FROM LINC.SFBDB_AAACM@DBLINK@" + 
			"   WHERE	DCO_USUAR = ?" + 
			"   	AND ANU_LOTE = ?" + 
			"   	AND ACO_ESTREG = ?" ;
	
	private static final String SELECT_SFBDB_ICPGE5 = 
			"SELECT ICO_ESTAD AS codEstado, " + 
			"		IFE_PAGO AS fechaPago, " + 
			"		IVA_PAGO AS valorPago," + 
			"		IFE_CARGA AS fechaCarga, " + 
			"		IHO_CARGA AS horaCarga" + 
			"	FROM LINC.SFBDB_ICPGE@DBLINK@" + 
			"	WHERE	ICO_ESTAD = ?" + 
			"		AND ICO_COLECT = ?" + 
			"		AND ICOTIPDOC = ?" + 
			"		AND INUREGCLI = ?" + 
			"		ORDER BY GLB_DTIME ASC";
	
	private static final String SELECT_SFBDB_IEAPC2 = 
			"SELECT IFE_VENCIM AS fechaVencimiento" + 
			"	FROM (SELECT IFE_VENCIM" + 
			"			FROM LINC.SFBDB_IEAPC@DBLINK@" + 
			"			WHERE	ICO_COLECT = ?" + 
			"				AND ICO_TIPDOC = ?" + 
			"				AND IFE_VENCIM >= ?" + 
			"			ORDER BY IFE_VENCIM DESC)"+ 
			"	WHERE ROWNUM <= ?";
	
	private static final String SELECT_SFBDB_AAMPR = "SELECT ACO_CONCE AS codConceptoProducto" + 
			"	FROM LINC.SFBDB_AAMPR@DBLINK@" + 
			"	WHERE ACO_PRODU = ?";
	
	private static final String SELECT_MADMIN_FNC_CORREL_CANAL = "SELECT MADMIN.FNC_CORREL_CANAL( ? ) as numTran FROM DUAL ";
	
	private static final String SELECT_SFBDB_AAMTC = "SELECT 1 AS senEncontrado, ANU_FOLIO AS numDocum" + 
			"	FROM LINC.SFBDB_AAMTC@DBLINK@" + 
			"	WHERE ACOTIPCOR = ?" + 
			"	AND ACO_OFICI = ?" + 
			"	AND ACO_PRODU = ?";
	
	private static final String UPDATE_SFBDB_AAMTC = "UPDATE LINC.SFBDB_AAMTC@DBLINK@" + 
			"	SET ANU_FOLIO = ?" + 
			"	WHERE ACOTIPCOR = ?" + 
			"	AND ACO_OFICI = ?" + 
			"	AND ACO_PRODU = ?";
	
	
	private static final String UPDATE_SFBDB_ICPGE2 = "UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET IFE_PAGO = ?," + 
			"	ICO_ESTAD = ?" + 
			"	WHERE ICO_ESTAD = ?" + 
			"	AND ICO_COLECT = ?" + 
			"	AND ICOTIPDOC = ?" + 
			"	AND INUREGCLI = ?" + 
			"	AND INUIDECLI = ?" +
			"   AND IFE_CARGA = ?" + 
			"	AND GLB_DTIME = ?";
	
	private static final String UPDATE_SFBDB_ICPGE3 = "UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET IFE_PAGO = ?" + 
			"	WHERE ICO_ESTAD = ?" + 
			"	AND ICO_COLECT = ?" + 
			"	AND ICOTIPDOC = ?" + 
			"	AND INUREGCLI = ?" + 
			"	AND INUIDECLI = ?" + 
			"	AND IFE_PAGO = ?" + 
			"	AND GLB_DTIME = ?";
	
	private static final String UPDATE_SFBDB_ICPGE4 = "UPDATE LINC.SFBDB_ICPGE@DBLINK@" + 
			"	SET INU_DOCUM = ?" + 
			"	WHERE ICO_ESTAD = ?" + 
			"   AND ICO_COLECT = ?" + 
			"   AND ICOTIPDOC = ?" + 
			"   AND INUREGCLI = ?" + 
			"	AND GLB_DTIME = ?";
	
	private static final String SELECT_SFBDB_AAACM2 = "SELECT ADEREGCAR AS numRegArchivoCargado" + 
			"   FROM LINC.SFBDB_AAACM@DBLINK@" + 
			"   WHERE DCO_USUAR = ?" + 
			"   AND ANU_LOTE = ?" + 
			"   AND ACOESTREG = ?";
	
	private static final String SELECT_GLBDTIME_DIF = "SELECT MADMIN.GENERATE_GLBDTIME_DIF as glbDtimeDAALA FROM DUAL";
	
	private static final String INSERT_SFBDB_ICATR = "INSERT INTO LINC.SFBDB_ICATR@DBLINK@(" + 
			"	GLB_DTIME, ICO_COLECT," + 
			"	ICO_ESTAD, ICOTIPDOC," + 
			"	DCO_OFICI, DCO_TERMI," + 
			"	DCO_USUAR, ICOTIPCLI," + 
			"	IFE_TRANS, IHO_TRANS," + 
			"	IMO_PAGO, INO_CLIEN," + 
			"	INUIDECLI, IFE_CARGA," + 
			"	IHO_CARGA, INUREGCLI," + 
			"	TNUDOCTRA, INU_REGIS," + 
			"	TSE_REVER, ICOTIPIDE," + 
			"	IFE_VENCI, IPEDECLAR," + 
			"	INUCTASFB, IMO_VALOR," + 
			"   TCO_CANAL, IFE_ENVIO," + 
			"   IHO_ENVIO, THO_CARGA)" +
			"	VALUES (" + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?)";
	
	private static final String INSERT_SFBDB_ICATR2 = "INSERT INTO LINC.SFBDB_ICATR@DBLINK@(" + 
			"	GLB_DTIME, ICO_COLECT," + 
			"	ICO_ESTAD, ICOTIPDOC," + 
			"	DCO_OFICI, DCO_TERMI," + 
			"	DCO_USUAR, ICOTIPCLI," + 
			"	IFE_TRANS, IHO_TRANS," + 
			"	IMO_PAGO, INO_CLIEN," + 
			"	INUIDECLI, IFE_CARGA," + 
			"	IHO_CARGA, INUREGCLI," + 
			"	TNUDOCTRA, INU_REGIS," + 
			"	TSE_REVER, ICOTIPIDE," + 
			"	IFE_VENCI, IPEDECLAR," + 
			"	INUCTASFB, IMO_VALOR)" + 
			"	VALUES (" + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?, " + 
			"	?, ?)";
	
	
	
	
	Logger logger = LoggerFactory.getLogger(PagaduriaGenericoServicio.class);
	
	@Override
	public Object procesar(Object objetoDom) throws ServicioException {

		logger.info(NOM_COD_SERVICIO + "Iniciando servicio...");

		logger.debug(NOM_COD_SERVICIO + "Creando objeto Datos Operacion ...");
		DatosOperacion datos = crearDatosOperacion();

		logger.debug(NOM_COD_SERVICIO + "Cast de objeto de dominio -> EntradasVariasPeticion");
		PagaduriaGenericoPeticion peticion = (PagaduriaGenericoPeticion) objetoDom;
		
try {
			
			logger.debug(NOM_COD_SERVICIO + "Iniciando validaciones iniciales de parametros...");
			validacionInicial(peticion);
			datos.agregarDato("peticion",peticion);
			datos.agregarPropiedadesDeObjeto(peticion);
			
			logger.debug(NOM_COD_SERVICIO + "Iniciando seguridad financiera de parametros...");
			seguridadTerminalFinancieros(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Definiendo variables que se usaran en el proceso");
			definirVariables(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando y validando datos de codigo de colector");
			recuperarValidarCodigoColector(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validando relacion de colector con Documento de Pago");
			validarRelacionColectorConDocumentoDePago(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Recuperando relacion transacion Anual");
			validarRelacionTransacionCausal(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validando relacion transaccion - subcausal");
			validarRelacionTransacionSubCausal(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validando parametros Colector");
			obtenerParametrosColector(datos);
			
			logger.debug(NOM_COD_SERVICIO + "validando numero de documento sea distinto de cero");
			validarNumDocumento(datos);
			
//			logger.debug(NOM_COD_SERVICIO + "Validar valor de movimiento igual a efectivo");
//			validarValorMovimiento(datos);
			
			logger.debug(NOM_COD_SERVICIO + "Validar parametros del registro");
			validarParametrosDelRegistro(datos);

			PagaduriaGenericoRespuesta respuesta = new PagaduriaGenericoRespuesta();
			
			if (UtileriaDeDatos.isNull(datos.obtenerValor("descripcionPagoVencido"))) {
				logger.debug(NOM_COD_SERVICIO + "Recuperar número de transacción.");
				recuperarNumeroTransaccion(datos);
				
				logger.debug(NOM_COD_SERVICIO + "Registrando en tabla SFBDB_AAATR");
				registarAAATR(datos);
				
				logger.debug(NOM_COD_SERVICIO + "Verificar cuenta parametrizada y concepto para hacer nota de credito");
				verificarCuentaParametrizadaYConcepto(datos);
				
				if((UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidarICPGE"),Constantes.SI))) {
					logger.debug(NOM_COD_SERVICIO + "Actualizando registro ICPGE");
					actualizarRegistroICPEG(datos);
				}
				
				logger.debug(NOM_COD_SERVICIO + "Insertando registro en tabla SFBDB_ICATR");
				insertarRegistroICATR(datos);
				
				logger.debug(NOM_COD_SERVICIO + "Asignación de variables de salida");
				asignacionVariablesSalida(datos);

				datos.llenarObjeto(respuesta);
				respuesta.setCodigo(0);
				respuesta.setDescripcion("EXITO");
			}else {
				respuesta.setDescripcion(datos.obtenerString("descripcionPagoVencido")); 
				respuesta.setCodigo(20016);
			}
			
			
			if(logger.isDebugEnabled()) {
				logger.debug(NOM_COD_SERVICIO + "RESPUESTA: {} ", respuesta);
			}
			
			return respuesta;
		
		} catch (ServicioException e) {
			logger.error("Ocurrio un error:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(e);
		} catch (TipoDatoException | ParseException e) {
			logger.error("Ocurrio un error inesperado:", e.getMessage(), e);
			throw manejarMensajeExcepcionServicio(new ServicioException(20001, NOM_COD_SERVICIO + "Error inesperado: " + e.getMessage()));
		} 
	}
	
	
	/**
	 * M&eacutetodo auxiliar para validar peticion recibida
	 * @param peticion
	 * @throws ServicioException
	 */
	private void validacionInicial(PagaduriaGenericoPeticion peticion) throws ServicioException {
		logger.debug(NOM_COD_SERVICIO + "Iniciando validacion de parametros");
		logger.debug(NOM_COD_SERVICIO + "Peticion recibida: {}", peticion);
		
		UtileriaDeParametros.validarParametro(peticion.getCodColector(), "codColector", TipoValidacion.ENTERO_MAYOR_IGUAL, new Integer[] {500});
		UtileriaDeParametros.validarParametro(peticion.getCodTipoDocumento(), "codTipoDocumento", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodOficinaTran(), "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodTran(), "codTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumDocumentoTran(), "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getSenSupervisor(), "senSupervisor", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getValorMovimiento(), "valorMovimiento", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCamposFormulario(), "camposFormulario", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getCodTerminal(), "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getCodCajero(), "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(peticion.getNumCaja(), "numCaja", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(peticion.getNumLote(), "numLote", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		if (!UtileriaDeDatos.isNull(peticion.getValorEfectivo())) {
			UtileriaDeParametros.validarParametro(peticion.getValorEfectivo(), "valorEfectivo", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		}
		UtileriaDeParametros.validarParametro(peticion.getGlbDtime(), "glbDtime", TipoValidacion.LONG_MAYOR_CERO);
	}

	private void seguridadTerminalFinancieros(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		logger.debug(NOM_COD_SERVICIO + "Invocando la funcion de soporte 'Seguridad para Terminales financieros' ...");
		seguridadTerminalesFinancieros(datos);
		
		Date fechaSistema = UtileriaDeDatos.fecha6ToDate(datos.obtenerInteger("fechaSistema"));
		Integer fechaSistemaAMD = UtileriaDeDatos.tofecha8yyyyMMdd(fechaSistema);
		
		datos.agregarDato("fechaSistemaAMD", fechaSistemaAMD);

	}
	
	/**
	 * M&eacutetodo auxiliar para definir variables necesarias para el proceso de Servicio Colector Generico
	 * @param datos
	 * @throws TipoDatoException 
	 */
	private void definirVariables(DatosOperacion datos) throws TipoDatoException {
		
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);
		
		datos.agregarDato("codPantalla", Constantes.ISPEC_AJ599);
		datos.agregarDato("codConcepto", Constantes.CONCEPTO_VE);
		datos.agregarDato("longCampo", new Integer(0));
		datos.agregarDato("numCredito", "0");
		datos.agregarDato("senCampo1", new Integer(0));
		datos.agregarDato("senCampo3", new Integer(0));
		datos.agregarDato("senCampo5", new Integer(0));
		datos.agregarDato("senCampo6", new Integer(0));
		datos.agregarDato("codTipoIdentificacion", "");
		datos.agregarDato("numIdentificacionCliente", "");
		datos.agregarDato("tipoCliente", new Integer(0));
		datos.agregarDato("periodoDeclaracion", "");
		datos.agregarDato("valorMoraAux", BigDecimal.ZERO);
		datos.agregarDato("senPlanilla", Constantes.NO);
		datos.agregarDato("senValidarICPGE", Constantes.SI);
		datos.agregarDato("desMensaje", "");
		
		if(UtileriaDeDatos.isEquals(peticion.getCodTipoDocumento(), new Integer(0))) {
			peticion.setCodTipoDocumento(new Integer(1));
			datos.agregarDato("peticion",peticion);
			datos.agregarPropiedadesDeObjeto(peticion);
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para validar Codigo de colector en SFBDB_IEMCO
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void recuperarValidarCodigoColector(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);

		logger.debug("Ejecutando sentencia SELECT SFBDB IEMCO, parametro: " + peticion.getCodColector());
		try {
			Map<String, Object> nomCodColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEMCO), peticion.getCodColector());	
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(nomCodColector);
			
			String codClienteIEMCO = adaptador.getString("codClienteIEMCO");
			BigDecimal valorMora =  adaptador.getBigDecimal("valorMora");
			BigDecimal porcentajeMora = adaptador.getBigDecimal("porcentajeMora");
			String aliasIEMCO = adaptador.getString("aliasIEMCO");
			
			if(UtileriaDeDatos.isNull(codClienteIEMCO)) {
				throw new ServicioException(20019, "No existe {}", "COLECTOR DEFINIDO EN IEMCO");
			}
			
			datos.agregarDato("valorMora", valorMora);
			datos.agregarDato("porcentajeMora", porcentajeMora);
			datos.agregarDato("aliasIEMCO", aliasIEMCO);
			datos.agregarDato("codClienteIEMCO", codClienteIEMCO);
			
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {}", "COLECTOR DEFINIDO EN IEMCO");
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para validar relacion colector con documento de pago
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarRelacionColectorConDocumentoDePago(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);

		logger.debug("Ejecutando sentencia SELECT SFBDB ICRDO, parametro: " + peticion.getCodColector() + "-"  + peticion.getCodTipoDocumento());
		try {
			Map<String, Object> nomCodColectorDocPago = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICRDO), 
														peticion.getCodColector(),peticion.getCodTipoDocumento());	
		
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(nomCodColectorDocPago);
			Integer codCausal = adaptador.getInteger("codCausal");
			Integer codSubCausal = adaptador.getInteger("codSubCausal");
			Integer  codTipoCorrelativo = adaptador.getInteger("codTipoCorrelativo");
			Integer senValidacionRegistro = adaptador.getInteger("senValidacionRegistro");
			Integer senAbonoCuenta = adaptador.getInteger("senAbonoCuenta");
			Integer senPagoExtemporaneo = adaptador.getInteger("senPagoExtemporaneo");
			String descTipoDocumento = adaptador.getString("descTipoDocumento");
			datos.agregarDato("codCausal", codCausal);
			datos.agregarDato("codSubCausal", codSubCausal);
			datos.agregarDato("codTipoCorrelativo", codTipoCorrelativo);
			datos.agregarDato("senValidacionRegistro", senValidacionRegistro);
			datos.agregarDato("senAbonoCuenta", senAbonoCuenta);
			datos.agregarDato("senPagoExtemporaneo", senPagoExtemporaneo);
			datos.agregarDato("descTipoDocumento", descTipoDocumento);
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {}", "RELACION TIPO DE DOCUMENTO CON COLECTOR");
		}
	}
	
	
	/**
	 * M&eacutetodo auxiliar para validar la relacion Transacion-causal
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarRelacionTransacionCausal(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);

		try {
			
			Object[] paramsAAMTM = {
					datos.obtenerString("codPantalla"),
					datos.obtenerInteger("codCausal")
			};
			
			logger.debug("Ejecutando sentencia SELECT SFBDB AAMTM, parametros: {}", paramsAAMTM);

			Integer senSubCausal= jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMTM),Integer.class, paramsAAMTM);	
			datos.agregarDato("senSubCausal", senSubCausal);
			
			if(!UtileriaDeDatos.isEquals(senSubCausal, Constantes.SI)) {
				throw new ServicioException(20005, "Señal incorrecta de uso subcausal en AAMTM");
			}
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {}", "CODIGO DE CAUSAL/TRANSACCION");
		}
	}
	
	
	/**
	 * M&eacutetodo auxiliar para recuperar y validar la relacion Transacion - subcausal
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarRelacionTransacionSubCausal(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		try {
			
			Object[] paramsAAMSM = {
					datos.obtenerString("codPantalla"),
					datos.obtenerInteger("codCausal") , 
					datos.obtenerInteger("codSubCausal")
			};
			
			logger.debug("Ejecutando sentencia SELECT SFBDB AAMTM, parametros: {}", paramsAAMSM);
			Map<String, Object> relTransSubCausal = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAMSM), paramsAAMSM);	
		
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(relTransSubCausal);
			Integer codProductoCta = adaptador.getInteger("codProductoCta");
			Integer codOficinaCta = adaptador.getInteger("codOficinaCta");
			Integer numCuenta = adaptador.getInteger("numCuenta");
			Integer digitoVerificadorCta = adaptador.getInteger("digitoVerificadorCta");
			datos.agregarDato("codProductoCta", codProductoCta);
			datos.agregarDato("codOficinaCta", codOficinaCta);
			datos.agregarDato("numCuenta", numCuenta);
			
			datos.agregarDato("digitoVerificadorCta", digitoVerificadorCta);
			Cliente cliente = new Cliente();
			String cuentaTransaccion = adaptador.getString("cuentaStr");
			datos.agregarDato("cuentaStr", cuentaTransaccion);
			Integer codConceptoCta = codProductoCta/100;
			if(UtileriaDeDatos.isEquals(codConceptoCta, Constantes.CONCEPTO_CC)) {
				CuentaCorriente pcc = recuperarDatosCuentaCorriente(cuentaTransaccion);
				cliente = recuperarDatosCliente(pcc.getCodCliente());
			}
			if(UtileriaDeDatos.isEquals(codConceptoCta, Constantes.CONCEPTO_AH)) {
				CuentaAhorro pca = recuperarDatosCuentaAhorro(cuentaTransaccion);
				cliente = recuperarDatosCliente(pca.getCodCliente());
			}
			datos.agregarDato("codSectorEconomicoCliente",cliente.getCodSectorEconomicoCliente());
			datos.agregarDato("codCliente",cliente.getCodCliente());
			datos.agregarDato("tipDocumentoCliente",cliente.getTipDocumentoCliente());
			datos.agregarDato("numDocumentoCliente",cliente.getNumDocumentoCliente());
			datos.agregarDato("cliente", cliente);
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe {} ", " CODIGO DE CAUSAL/TRANSACCION");
		}
	}
	
	
	
	/**
	 * M&eacutetodo auxiliar para obtener parametros del colector
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void obtenerParametrosColector(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);
		List<Map<String, Object>>  listadoCamposColector = null;

		try {
			
			Object[] paramsIEAGE = {
					peticion.getCodColector(),
					new Integer(0)
			};
			
			logger.debug("Ejecutando sentencia SELECT SFBDB IEAGE, parametros: {}", paramsIEAGE);
			listadoCamposColector = jdbcTemplate.queryForList(query(SELECT_SFBDB_IEAGE), paramsIEAGE );
			
			if(UtileriaDeDatos.listIsEmptyOrNull(listadoCamposColector)) {
				throw new ServicioException(20019, "No existe parametros de colector.", "PARAMETROS DE COLECTOR");
			}
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No existe parametros de colector.", "PARAMETROS DE COLECTOR");
		}
		
		AdaptadorDeMapa adaptador;
		Integer posicionAnterior = 0;
		Boolean fechaValida = Boolean.FALSE;
		Integer longCampo = 0;
		String numCredito = null;
		BigDecimal valorMoraAux = BigDecimal.ZERO;
		
			for (Map<String, Object> map : listadoCamposColector) {
				adaptador = UtileriaDeDatos.adaptarMapa(map);
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "NOCREDITO") && 
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
					longCampo = adaptador.getInteger("longitudCampo");
					numCredito = StringUtils.leftPad(peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo), 20, '0');
					datos.agregarDato("numCredito", numCredito);
					if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
						datos.agregarDato("senCampo1", Constantes.SI); 
					}
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "ICOTIPIDE") &&
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
					longCampo = adaptador.getInteger("longitudCampo");
					datos.agregarDato("codTipoIdentificacion", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "INUIDECLI") && 
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
					longCampo = adaptador.getInteger("longitudCampo");
					datos.agregarDato("numIdentificacionCliente", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
					if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
						datos.agregarDato("senCampo3", Constantes.SI);
					}
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "ICOTIPCLI") && 
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
					longCampo = adaptador.getInteger("longitudCampo");
					datos.agregarDato("tipoCliente", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "IPE-DECLAR") && 
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
					longCampo = adaptador.getInteger("longitudCampo");
					datos.agregarDato("periodoDeclaracion", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
					if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
						datos.agregarDato("senCampo5", Constantes.SI);
					}
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "IVALMORA") &&
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
					longCampo = adaptador.getInteger("longitudCampo");
					valorMoraAux = UtileriaDeDatos.formatBigDecimal(peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
					datos.agregarDato("valorMoraAux", valorMoraAux);
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "CARGA") && 
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senArchivo"), Constantes.SI)) {
					datos.agregarDato("senPlanilla", adaptador.getInteger("senArchivo"));
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "ICPGE") && 
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senArchivo"), Constantes.NO)) {
					datos.agregarDato("senValidarICPGE", adaptador.getInteger("senArchivo"));
					posicionAnterior += longCampo;
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "INO-CLIEN") && 
				   UtileriaDeDatos.isEquals(adaptador.getInteger("senVisible"), Constantes.SI)) {
					longCampo = adaptador.getInteger("longitudCampo");
					datos.agregarDato("nomCliente", peticion.getCamposFormulario().substring(posicionAnterior, posicionAnterior + longCampo));
					if(UtileriaDeDatos.isEquals(adaptador.getInteger("senObligatorio"), Constantes.SI)) {
						datos.agregarDato("senCampo6", Constantes.SI);
					}
					posicionAnterior += longCampo;
				}
				
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "MMAAXX")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"MMAA");				
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "DDMMAA")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"DDMMAA");
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "AAMMDD")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"AAMMDD");
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "MMAAAA")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"MMAAAA");
				}
				if(UtileriaDeDatos.isEquals(adaptador.getString("nombreCampo"), "AAAAMM")) {
					fechaValida = UtileriaDeDatos.validarFormatoFecha(adaptador.getString("periodoDeclaracion"),"AAAAMM");
				}
			}
			
			logger.debug("Validaciones de colector");
			
			if(UtileriaDeDatos.isEquals(Constantes.SI, datos.obtenerInteger("senCampo1")) && 
			   UtileriaDeDatos.isEquals(Integer.parseInt(datos.obtenerString("numCredito")), new Integer(0))) {
				throw new ServicioException(20589, "Numero incorrecto");
			}
			if(UtileriaDeDatos.isEquals(Constantes.SI, datos.obtenerInteger("senCampo3")) && 
			   UtileriaDeDatos.isEquals("0", datos.obtenerString("numIdentificacionCliente"))){
				throw new ServicioException(20589, "Numero incorrecto");
			}
			if(!fechaValida  && 
			   UtileriaDeDatos.isEquals(Constantes.SI, datos.obtenerInteger("senCampo5"))) {
				throw new ServicioException(20003, "Fecha incorrecta {}", " O PERIODO NO VALIDO");
			}
	}
	
	
	/**
	 * M&eacutetodo auxiliar para validar que el numero de documento sea distinto de cero
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarNumDocumento(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		Integer codTipoCorrelativo = datos.obtenerInteger("codTipoCorrelativo");
		Integer numDocumentoTran = datos.obtenerInteger("numDocumentoTran");
		
		if(UtileriaDeDatos.isEquals(new Integer(0), codTipoCorrelativo) && UtileriaDeDatos.isEquals(new Integer(0), numDocumentoTran)) {
			throw new ServicioException(20004, "Documento Incorrecto");
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para asegurarse que el valor de movimiento sea igual a efectivo
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
//	private void validarValorMovimiento(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
//		
//		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);
//		
//		if(!(UtileriaDeDatos.isEquals(peticion.getValorEfectivo(),peticion.getValorMovimiento()))) {
//			throw new ServicioException(20286, "Valor del movimiento no está cuadrado");
//		}
//	}
	
	/**
	 * M&eacutetodo auxiliar para validar parametros del registro
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void validarParametrosDelRegistro(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);
		
		String nomCliente = "";
		String numRegistroCliente = "";
		String codEstado = "";
		Integer fechaPago = 0;
		BigDecimal valorPago = null;
		Integer tipoClienteICPGE = null;
		Integer fechaCarga = null;
		Integer horaCarga = null;
		Integer fechaVencimiento = 0;
		String numDocumento = "";
		String impresionFISDL = "";
		BigDecimal totalPlan = BigDecimal.ZERO;
		String convenio = "";
		
		Integer fechaUltimaHabilAMD = 0;
		recuperarUltimoDiaHabilMes(datos);
		fechaUltimaHabilAMD = datos.obtenerInteger("fechaUltimaHabilMes");
		
		if(UtileriaDeDatos.isGreater(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_DIFERENTE_CUOTA) || 
		   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), new Integer(0))) {
			throw new ServicioException(20018, "Valor incorrecto {}", "PARAMETRO DE VALIDACION DE REGISTRO") ;
		}
		
		String codPlanilla = "";
		String programaFISDL = "";
		
		//Nuevas variables globales para senPlanilla
		BigDecimal montoAux = BigDecimal.ZERO;
		
		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"), Constantes.NO)) {
			logger.debug("Iniciando condicion senPlantilla = NO");
			
			String codEstadoAux = "";
			
			if(UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL) || 
			   UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL_INDEMNIZATORIO)) {
				
				try {
					
					Object[] paramsICPGE = {
							 Constantes.GP_INGRESO,
							 peticion.getCodColector(),
							 peticion.getCodTipoDocumento(),
							 StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
							 StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0'),
							 peticion.getGlbDtime()
					};
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE));
					Map<String, Object> regCuotaColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICPGE),paramsICPGE);	
					AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(regCuotaColector);
					nomCliente = adaptador.getString("nomCliente");
					numRegistroCliente = adaptador.getString("numRegistroCliente");
					codEstado =  adaptador.getString("codEstado");
					fechaPago =  adaptador.getInteger("fechaPago");
					valorPago =  adaptador.getBigDecimal("valorPago");
					tipoClienteICPGE =  adaptador.getInteger("tipoClienteICPGE");
					fechaCarga =  adaptador.getInteger("fechaCarga");
					horaCarga = adaptador.getInteger("horaCarga");	
					fechaVencimiento = adaptador.getInteger("fechaVencimiento");	
					numDocumento = adaptador.getString("numDocumento");
					convenio = adaptador.getString("convenio");
					codEstadoAux = Constantes.GP_INGRESO;
					datos.agregarDato("tipoClienteICPGE", adaptador.getInteger("tipoClienteICPGE"));
					datos.agregarDato("fechaCarga",fechaCarga);
					datos.agregarDato("horaCarga",horaCarga);
					datos.agregarDato("fechaVencimiento",fechaVencimiento);
				}catch (EmptyResultDataAccessException e) {
					
					try {
						Object[] paramsICPGE = {
								 Constantes.GP_PAGO,
								 peticion.getCodColector(),
								 peticion.getCodTipoDocumento(),
								 StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
								 StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0'),
								 datos.obtenerInteger("fechaSistemaAMD"),
								 peticion.getGlbDtime()
						};
						
						logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE));
						Map<String, Object> regCuotaColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICPGE2),paramsICPGE);	
						AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(regCuotaColector);
						nomCliente = adaptador.getString("nomCliente");
						numRegistroCliente = adaptador.getString("numRegistroCliente");
						codEstado =  adaptador.getString("codEstado");
						fechaPago =  adaptador.getInteger("fechaPago");
						valorPago =  adaptador.getBigDecimal("valorPago");
						tipoClienteICPGE =  adaptador.getInteger("tipoClienteICPGE");
						fechaCarga =  adaptador.getInteger("fechaCarga");
						horaCarga = adaptador.getInteger("horaCarga");	
						fechaVencimiento = adaptador.getInteger("fechaVencimiento");	
						numDocumento = adaptador.getString("numDocumento");
						convenio = adaptador.getString("convenio");
						codEstadoAux = Constantes.GP_PAGO;
						datos.agregarDato("tipoClienteICPGE", adaptador.getInteger("tipoClienteICPGE"));
						datos.agregarDato("fechaCarga",fechaCarga);
						datos.agregarDato("horaCarga",horaCarga);
						datos.agregarDato("fechaVencimiento",fechaVencimiento);
					}catch (EmptyResultDataAccessException err) {
						throw new ServicioException(20019, "No existe {}", "REGISTRO DE CUOTA BASE-COLECTOR"  +
								datos.obtenerString("numCredito") + datos.obtenerString("numIdentificacionCliente")) ;
					}
				}
			}else {
				if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidarICPGE"), Constantes.SI)) {
					logger.debug("Iniciando condicion senValidarICPGE = SI");
					// codigo original
					try {

						Object[] paramsICPGE = {
								Constantes.GP_INGRESO,
								peticion.getCodColector(),
								peticion.getCodTipoDocumento(),
								StringUtils.leftPad(datos.obtenerString("numCredito"),20,'0'),
								peticion.getGlbDtime()
						};
	
						logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE));
						Map<String, Object> regCuotaColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICPGE3),paramsICPGE);	
						AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(regCuotaColector);
						nomCliente = adaptador.getString("nomCliente");
						numRegistroCliente = adaptador.getString("numRegistroCliente");
						codEstado =  adaptador.getString("codEstado");
						fechaPago =  adaptador.getInteger("fechaPago");
						valorPago =  adaptador.getBigDecimal("valorPago");
						tipoClienteICPGE =  adaptador.getInteger("tipoClienteICPGE");
						fechaCarga =  adaptador.getInteger("fechaCarga");
						horaCarga = adaptador.getInteger("horaCarga");	
						fechaVencimiento = adaptador.getInteger("fechaVencimiento");	
						numDocumento = adaptador.getString("numDocumento");
						convenio = adaptador.getString("convenio");
						codEstadoAux = Constantes.GP_INGRESO;
						datos.agregarDato("tipoClienteICPGE", adaptador.getInteger("tipoClienteICPGE"));
						datos.agregarDato("fechaCarga",fechaCarga);
						datos.agregarDato("horaCarga",horaCarga);
						datos.agregarDato("fechaVencimiento",fechaVencimiento);
					}catch (EmptyResultDataAccessException e) {
	
						try {
							Object[] paramsICPGE = {
									Constantes.GP_PAGO,
									peticion.getCodColector(),
									peticion.getCodTipoDocumento(),
									StringUtils.leftPad(datos.obtenerString("numCredito"),20,'0'),
									peticion.getGlbDtime()
							};
	
							logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE));
							Map<String, Object> regCuotaColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICPGE4),paramsICPGE);	
							AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(regCuotaColector);
							nomCliente = adaptador.getString("nomCliente");
							numRegistroCliente = adaptador.getString("numRegistroCliente");
							codEstado =  adaptador.getString("codEstado");
							fechaPago =  adaptador.getInteger("fechaPago");
							valorPago =  adaptador.getBigDecimal("valorPago");
							tipoClienteICPGE =  adaptador.getInteger("tipoClienteICPGE");
							fechaCarga =  adaptador.getInteger("fechaCarga");
							horaCarga = adaptador.getInteger("horaCarga");	
							fechaVencimiento = adaptador.getInteger("fechaVencimiento");	
							numDocumento = adaptador.getString("numDocumento");
							convenio = adaptador.getString("convenio");
							codEstadoAux = Constantes.GP_PAGO;
							datos.agregarDato("tipoClienteICPGE", adaptador.getInteger("tipoClienteICPGE"));
							datos.agregarDato("fechaCarga",fechaCarga);
							datos.agregarDato("horaCarga",horaCarga);
							datos.agregarDato("fechaVencimiento",fechaVencimiento);
							
							if(!UtileriaDeDatos.mapIsEmptyOrNull(regCuotaColector)) {
								throw new ServicioException(20020, "Ya existe {}", "REGISTRO SUBSIDIO PAGADO");
							}
	
						}catch (EmptyResultDataAccessException erdae) {
							throw new ServicioException(20020, "Ya existe {}", "REGISTRO SUBSIDIO PAGADO");
						}
					}
				} else {
					logger.debug("Iniciando condicion senValidarICPGE = NO");
					datos.agregarDato("codEstado", Constantes.GP_INGRESO);
					datos.agregarDato("fechaPago", 0);
					datos.agregarDato("tipoClienteICPGE", 0);
					datos.agregarDato("fechaCarga", 0);
					datos.agregarDato("horaCarga", 0);
					codEstado = Constantes.GP_INGRESO;
					fechaPago = 0;
				}
			}
			
			logger.debug("Valor de cuota exacta, Permite realizar un solo pago igual a la cuota");
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_CUOTA_EXACTA) ||
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_MAYOR_CUOTA) ||
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_DIFERENTE_CUOTA)) {
				
				if(UtileriaDeDatos.isEquals(codEstado, "V")) {
					throw new ServicioException(20016, "Estado incorrecto {}", " PAGO VENCIDO");
				}
				
				if(UtileriaDeDatos.isEquals(codEstado, "P")) {
					throw new ServicioException(20020, "Ya existe {}", " REGISTRO PAGADO");
				}
				
				logger.debug("codEstado: {}", codEstado);
				logger.debug("fechaPago: {}", fechaPago);

				if(!UtileriaDeDatos.isEquals(codEstado, "I") || !UtileriaDeDatos.isEquals(fechaPago, new Integer(0))) {
					throw new ServicioException(20020, "Ya existe {}", " SUBSIDIO PAGADO EN BASE DE DATOS");
				}
				
				if(UtileriaDeDatos.isGreater(fechaVencimiento, new Integer(0)) &&
				   UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento)) {
					
					Object[] params_ICPGE = {
							"V",
							codEstadoAux,
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
							StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0'),
							fechaCarga,
							peticion.getGlbDtime()
					};

					logger.debug("Ejecutando sentencia UPDATE LINC SFBDB ICPGE, parametros: {}", Arrays.toString(params_ICPGE));
					Integer rowcount = getJdbcTemplate().update(query(UPDATE_SFBDB_ICPGE), params_ICPGE);
					
					if(UtileriaDeDatos.isGreaterThanZero(rowcount)) {
						logger.info("SE ENCONTRO UN PAGO VENCIDO, PARA PAGAR, CONSULTE DE NUEVO");
						datos.agregarDato("descripcionPagoVencido", "ESTADO INCORRECTO, PARA PAGAR, CONSULTE DE NUEVO");
						return;
					}
					
					if(UtileriaDeDatos.isEquals(rowcount, new Integer(0))) {
						throw new ServicioException(20016, "Estado incorrecto {}", 
								" PARA PAGAR, CONSULTE DE NUEVO - NO PUDO ACTUALIZARSE ESTADO DE REGISTRO A VENCIDO");
					}
				}
			}
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_CUOTA_EXACTA) &&
			  !UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), valorPago)) {
				throw new ServicioException(20018, "Valor incorrecto {}", "DIFIERE DEL VALOR CUOTA A PAGAR");
			}
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_MAYOR_CUOTA) &&
			  UtileriaDeDatos.isGreater(valorPago,peticion.getValorMovimiento())) {
						throw new ServicioException(20018, "Valor incorrecto {}", "ES MENOR AL VALOR CUOTA A PAGAR");
			}
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_PAGO) &&
			  UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("valorMoraAux"), BigDecimal.ZERO)) {
						throw new ServicioException(20019, "No Existe {}", "NO DEBE INGRESAR MORA");
			}
			
			if(UtileriaDeDatos.isGreater(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO) ||
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), new Integer(0))) {
						throw new ServicioException(20018, "Valor incorrecto {}", "PARAMETRO PAGO EXTEMPORÁNEO");
			}
			
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA) ||
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)) {
				
				try {
					
					Object[] params_IEAPC = {
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							fechaUltimaHabilAMD,
							new Integer(1)
					};
					
					logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
					fechaVencimiento = jdbcTemplate.queryForObject(query(SELECT_SFBDB_IEAPC), Integer.class, params_IEAPC);
				} catch (EmptyResultDataAccessException e) {
					
					try {
						
						Object[] params_IEAPC = {
								peticion.getCodColector(),
								Constantes.GP_CODIGO_TIPO_DOCUMENTO,
								fechaUltimaHabilAMD,
								new Integer(1)
						};
						
						logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
						fechaVencimiento = jdbcTemplate.queryForObject(query(SELECT_SFBDB_IEAPC), Integer.class, params_IEAPC);
					} catch (EmptyResultDataAccessException e2) {
						throw new ServicioException(20019, "No Existe {}", "TIPO DOCUMENTO CON PARAMETRO VALIDACION DE FECHA");
					}
				}
				
				if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("valorMoraAux"), BigDecimal.ZERO) &&
				   !UtileriaDeDatos.isEquals(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) &&
				   UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {
					throw new ServicioException(20018, "Valor incorrecto {}", "MORA NO ES REQUERIDA");
				}
				
				if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) &&
						UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)) {
					throw new ServicioException(20003, "Fecha incorrecta {}", " VENCIDA PARA REALIZAR PAGO");
				}

				if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) &&
						UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {

					logger.debug("Inicio validacion fechaSistemaAMD  && senPagoExtemporaneo");
					if(UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO) &&
							UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("valorMora"), BigDecimal.ZERO)) {
						throw new ServicioException(20019, "No Existe {}", "PARAMETRO PARA COBRAR MORA");		
					}


					montoAux = peticion.getValorMovimiento();
					montoAux = montoAux.subtract(datos.obtenerBigDecimal("valorMora"));

					if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO)) {

						BigDecimal porcentaje = datos.obtenerBigDecimal("porcentajeMora");
						porcentaje = porcentaje.divide(new BigDecimal(100));
						BigDecimal moraCalculada = montoAux.multiply(porcentaje).setScale(2,  BigDecimal.ROUND_HALF_UP);

						if(!UtileriaDeDatos.isEquals(moraCalculada, datos.obtenerBigDecimal("valorMoraAux"))) {
							throw new ServicioException(20018, "Valor incorrecto {}", "DE RECARGO POR MORA %");
						}
					}

					if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("valorMora"), BigDecimal.ZERO ) && 
							!UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("valorMoraAux"), datos.obtenerBigDecimal("valorMora"))
							) {
						throw new ServicioException(20018, "Valor incorrecto {}", "DE RECARGO POR MORA");
					}
				}
				
			}
			
			logger.debug("transaccionesColector");
			datos.agregarDato("numRegistroCliente", numRegistroCliente);
			
				Map<String, Object> transaccionesColector = transaccionesColector(datos);	
				if(!UtileriaDeDatos.mapIsEmptyOrNull(transaccionesColector)) {
				throw new ServicioException(20020, "Ya existe {}", "TRANSACCIÓN DE SUBSIDIO PAGADO");
				}
			
			
			
			if(UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL) ||
			   UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL_INDEMNIZATORIO)) {
				
				impresionFISDL = convenio + "," + numDocumento.substring(0)  + "," + peticion.getCodTipoDocumento() +
						"," + datos.obtenerString("descTipoDocumento");
				
				datos.agregarDato("impresionFISDL", impresionFISDL);
				
			}
			
			programaFISDL = datos.obtenerString("descTipoDocumento");
			
			datos.agregarDato("codPlanilla", codPlanilla);
			datos.agregarDato("programaFISDL", programaFISDL);
			logger.debug("finalizando condicion senPlantilla = NO");
			datos.agregarDato("convenio", convenio);
			datos.agregarDato("codEstadoAux", codEstadoAux);
		}//Fin SenPlanilla = NO
		
		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"), Constantes.SI)) {
			
			//Agregando Validacion
			UtileriaDeParametros.validarParametro(peticion.getNumLote(), "numLote", TipoValidacion.OBJETO_NULO);
			UtileriaDeParametros.validarParametro(peticion.getNumLote(), "numLote", TipoValidacion.ENTERO_MAYOR_CERO);
			
			if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_CUOTA_EXACTA) ||
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_MAYOR_CUOTA) ||
			   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_DIFERENTE_CUOTA)) {
				
				logger.debug("Ejecutando listadoCargasArchivo");
				
				List<Map<String, Object>> cargasArchivoList = null;
				try {
					cargasArchivoList = cargasArchivo(datos);	
					if(UtileriaDeDatos.listIsEmptyOrNull(cargasArchivoList)) {
						throw new ServicioException(20019, "No existe {}", "ARCHIVO CARGADO");
					}
				} catch (EmptyResultDataAccessException erdae) {
					throw new ServicioException(20019, "No existe {}", "ARCHIVO CARGADO");
				}
				
				AdaptadorDeMapa	adaptador = null;
				String codColectorAux = "";
				String numCreditoAux = "";
				String valorPagoAux = "";
				BigDecimal valorMoraAux = datos.obtenerBigDecimal("valorMoraAux");
				BigDecimal porcentaje = BigDecimal.ZERO;
				BigDecimal moraCalculada = BigDecimal.ZERO;
				for (Map<String, Object> map : cargasArchivoList) {
					
					adaptador = UtileriaDeDatos.adaptarMapa(map);
					codColectorAux = adaptador.getString("numRegArchivoCargado").substring(0, 3);
					numCreditoAux = adaptador.getString("numRegArchivoCargado").substring(25, 45);
					numCreditoAux = StringUtils.leftPad(numCreditoAux, 20, "0");
					
					try {
						
						Object[] params_ICPG = {
								Constantes.GP_INGRESO,
								codColectorAux,
								peticion.getCodTipoDocumento(),
								numCreditoAux
						};
						
						logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICPG, parametros: {}", Arrays.toString(params_ICPG));
						Map<String, Object> mapICPGE = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICPGE5), params_ICPG);
						
					} catch (EmptyResultDataAccessException e) {
						throw new ServicioException(20019, "No existe {}", "SUBSIDIO DOCUMENTO NO." + numCreditoAux + " EN ARCHIVO CARGADO");
					}
					
					// ToDo: validar recuperacion de valorPagoAux con dos decimales.
					valorPagoAux = adaptador.getString("numRegArchivoCargado").substring(57, 68);
					totalPlan = totalPlan.add(UtileriaDeDatos.formatBigDecimal(valorPagoAux));
					
					if((UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_CUOTA_EXACTA) || 
					   UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_MAYOR_CUOTA)) 
													&&
					   (!UtileriaDeDatos.isEquals(codEstado, "I") || !UtileriaDeDatos.isEquals(fechaPago, new Integer(0))) ) {
						throw new ServicioException(20020, "Ya existe {}", "SUBSIDIO PAGADO EN BASE DE DATOS");
					}
					
					logger.debug("Valor de cuota exacta");
					if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_CUOTA_EXACTA) &&
					  !UtileriaDeDatos.isEquals(UtileriaDeDatos.formatBigDecimal(valorPagoAux), valorPago)) {
						throw new ServicioException(20018, "Valor incorrecto {}", "DIFIERE VALOR SUBSIDIO A PAGAR");
					}
					
					
					logger.debug("Permite realizar un pago igual o mayor a la cuota");
					if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GP_PAGO_MAYOR_CUOTA) &&
					   UtileriaDeDatos.isGreater(valorPago, UtileriaDeDatos.formatBigDecimal(valorPagoAux))) {
						throw new ServicioException(20018, "Valor incorrecto {}", "ES MENOR VALOR SUBSIDIO A PAGAR");
					}
					
					// ToDo: validar recuperacion de valorMoraAux tomando en cuenta decimales.
					valorMoraAux = UtileriaDeDatos.formatBigDecimal(adaptador.getString("numRegArchivoCargado").substring(68,76));
					
					if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_PAGO) &&
					   UtileriaDeDatos.isGreater(valorMoraAux, BigDecimal.ZERO)) {
						throw new ServicioException(20019, "No Existe {}", "NO DEBE INGRESAR MORA");
					}
					
					if(UtileriaDeDatos.isGreater(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO) || 
					   UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), new Integer(0))) {
						throw new ServicioException(20018, "Valor incorrecto {}", "PARAMETRO PAGO EXTEMPORANEO");
					}
					
					if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA) ||
					   UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)) {
						
						try {
							
							Object[] params_IEAPC = {
									peticion.getCodColector(),
									peticion.getCodTipoDocumento(),
									fechaUltimaHabilAMD,
									new Integer(1)
							};
							
							logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
							Map<String, Object> mapIEAPC = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEAPC2), params_IEAPC);
							adaptador = UtileriaDeDatos.adaptarMapa(mapIEAPC);
							fechaVencimiento = adaptador.getInteger("fechaVencimiento");
							
						} catch (EmptyResultDataAccessException e) {
							
							try {
								
								Object[] params_IEAPC = {
										peticion.getCodColector(),
										Constantes.GP_CODIGO_TIPO_DOCUMENTO,
										fechaUltimaHabilAMD,
										new Integer(1)
								};
								
								logger.debug("Ejecutando sentencia SELECT LINC SFBDB IEAPC, parametros: {}", Arrays.toString(params_IEAPC));
								Map<String, Object> mapICPGE = jdbcTemplate.queryForMap(query(SELECT_SFBDB_IEAPC2), params_IEAPC);
								adaptador = UtileriaDeDatos.adaptarMapa(mapICPGE);
								fechaVencimiento = adaptador.getInteger("fechaVencimiento");
								
							} catch (EmptyResultDataAccessException er) {
								throw new ServicioException(20019, "No Existe {}", "TIPO DOCUMENTO CON PARAMETRO VALIDACION DE FECHA");							}
						}
						
						if(UtileriaDeDatos.isGreater(valorMoraAux, BigDecimal.ZERO) &&
						  !UtileriaDeDatos.isEquals(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) &&
						   UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {
							throw new ServicioException(20018, "Valor incorrecto {}", ", MORA NO ES REQUERIDA");
						}
						
						if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) &&
								UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_FECHA_VENCIMIENTO)) {
							throw new ServicioException(20003, "Fecha incorrecta {}", " VENCIDA PARA REALIZAR PAGO");
						}

						if(UtileriaDeDatos.isGreater(datos.obtenerInteger("fechaSistemaAMD"), fechaVencimiento) &&
								UtileriaDeDatos.isEquals(datos.obtenerInteger("senPagoExtemporaneo"), Constantes.SENIAL_VALIDA_VENCIMIENTO_MORA)) {

							logger.debug("Inicio validacion fechaSistemaAMD  && senPagoExtemporaneo");
							if(UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO) &&
								UtileriaDeDatos.isEquals(datos.obtenerBigDecimal("valorMora"), BigDecimal.ZERO)) {
								throw new ServicioException(20019, "No Existe {}", "PARAMETRO PARA COBRAR MORA");		
							}

							montoAux = peticion.getValorMovimiento();
							montoAux = montoAux.subtract(valorMoraAux);

							if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("porcentajeMora"), BigDecimal.ZERO)) {

								porcentaje = datos.obtenerBigDecimal("porcentajeMora");
								porcentaje = porcentaje.divide(new BigDecimal(100));

								moraCalculada = montoAux.multiply(porcentaje).setScale(2, BigDecimal.ROUND_HALF_UP);

								if(!UtileriaDeDatos.isEquals(moraCalculada, valorMoraAux)) {
									throw new ServicioException(20018, "Valor incorrecto {}", "DE RECARGO POR MORA %");
								}
							}

							if(UtileriaDeDatos.isGreater(datos.obtenerBigDecimal("valorMora"), BigDecimal.ZERO ) && 
									!UtileriaDeDatos.isEquals(valorMoraAux, datos.obtenerBigDecimal("valorMora"))
									) {
								throw new ServicioException(20018, "Valor incorrecto {}", "DE RECARGO POR MORA");
							}
						}
					}
						logger.debug("fin validacion fechaSistemaAMD && senPagoExtemporaneo");
				
						
						Map<String, Object> transaccionesColector = transaccionesColector(datos);	
						if(!UtileriaDeDatos.mapIsEmptyOrNull(transaccionesColector)) {
							throw new ServicioException(20020, "Ya existe {}", "TRANSACCIÓN DE SUBSIDIO PAGADO");
						}
					}
				
					if(!UtileriaDeDatos.isEquals(peticion.getValorMovimiento(), totalPlan)) {
						throw new ServicioException(20224, "Valor de la transacción incorrecto {} ", "CON TOTAL ARCHIVO CARGADO");
					}
					
					nomCliente = datos.obtenerString("aliasIEMCO");
					
				}
				
			}
		
		logger.debug("Fin validacion senPlanilla = SI"); 
		datos.agregarDato("fechaVencimiento", fechaVencimiento);
		datos.agregarDato("nomCliente", nomCliente);
	
	}
	
	
	private Map<String, Object> transaccionesColector(DatosOperacion datos) throws TipoDatoException, ServicioException{

		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);
		Map<String, Object> transaccionColector = null;
		try {
			Object[] paramsICATR = {
					datos.obtenerInteger("fechaSistemaAMD"),
					peticion.getCodOficinaTran(),
					peticion.getCodTerminal(),
					peticion.getCodCajero(),
					StringUtils.leftPad(datos.obtenerString("numRegistroCliente"), 20, '0'),
					peticion.getNumDocumentoTran(),
					peticion.getValorMovimiento(),
					new Integer(0),
					new Integer(1),
			};

			logger.debug("Ejecutando sentencia SELECT LINC SFBDB ICATR, parametros: {}", Arrays.toString(paramsICATR));
			transaccionColector = jdbcTemplate.queryForMap(query(SELECT_SFBDB_ICATR), paramsICATR);

		} catch (EmptyResultDataAccessException ignored) {
		}
		return transaccionColector;
	}
	
	private List<Map<String, Object>> cargasArchivo(DatosOperacion datos) throws TipoDatoException, EmptyResultDataAccessException{
		
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);
		Object[] paramsAAACM = {
				peticion.getCodCajero(),
				peticion.getNumLote(),
				Constantes.GP_ESTADO_COLECTOR_PENDIENTE,
				Constantes.GP_PAGO_DIFERENTE_CUOTA
		};

		logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAACM, parametros: {}", Arrays.toString(paramsAAACM));
		List<Map<String, Object>> listadocargasArchivo = jdbcTemplate.queryForList(query(SELECT_SFBDB_AAACM), paramsAAACM);
		return listadocargasArchivo;
	}
	
	
	/**
	 * M&eacutetodo auxiliar recuperar Numero Transaccion
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void recuperarNumeroTransaccion(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		logger.debug("Ejecutando sentencia SELECT MADMIN FNC CORREL CANAL, parametro:" + Constantes.VENTANILLA);
		try {
			Map<String, Object> numeroTransacion = jdbcTemplate.queryForMap(query(SELECT_MADMIN_FNC_CORREL_CANAL),Constantes.VENTANILLA);
			AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(numeroTransacion);
			Integer numTran = adaptador.getInteger("numTran");
			datos.agregarDato("numTran", numTran);
		}catch (EmptyResultDataAccessException e) {
			throw new ServicioException(20019, "No Existe {}", "NUMERO DE TRANSACCION");
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para registrar en AAATR
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void registarAAATR(DatosOperacion datos) throws ServicioException, TipoDatoException {

		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);

		logger.debug("Preparando datos para registrar en tabla AAATR");
		//codCasual recuperada en  Validar relación colector con documento de pago
		datos.agregarDato("codConcepto", datos.obtenerInteger("codConcepto"));
		datos.agregarDato("codOficinaCta", peticion.getCodOficinaTran());
		datos.agregarDato("codOficina", peticion.getCodOficinaTran());
		//codProductoCta recuperada en Validar relación transacción-subcausal
		datos.agregarDato("numCuenta", 0);
		//digitoVerificadorCta recuperada en Validar relación transacción-subcausal
		datos.agregarDato("codOficinaTran", peticion.getCodOficinaTran());
		datos.agregarDato("codTerminal", peticion.getCodTerminal());
		//fecha relativa recuperada de Seguridad Terminales Financieros
		//hora sistema recuperada de Seguridad Terminales Financieros
		//numTran recuperada en Recuperar Numero Transaccion
		datos.agregarDato("numDocumentoTran", peticion.getNumDocumentoTran());
		//codCompania recuperada de Seguridad Terminales Financieros
		datos.agregarDato("codMoneda", 2);
		datos.agregarDato("numCaja", peticion.getNumCaja());
		datos.agregarDato("montoIva", null);
		datos.agregarDato("codTran", peticion.getCodTran());
		datos.agregarDato("codCajero", peticion.getCodCajero());
		datos.agregarDato("codDebCre", Constantes.DEBITO);
		datos.agregarDato("numSecuenciaCupon", null);
		datos.agregarDato("valorImpuestoVenta", null);
		//codSectorEconomicoCliente recuperada en Validar relación transacción-subcausal 
		datos.agregarDato("numDiasAtras", null);
		//fechaSistema recuperada de Seguridad Terminales Financieros
		//fechaTran recuperada de Seguridad Terminales Financieros
		datos.agregarDato("horaTran", datos.obtenerInteger("horaSistema"));
		datos.agregarDato("numDocumentoReversa", null);
		datos.agregarDato("numDocumentoReversa", null);
		datos.agregarDato("saldoAnterior", null);
		datos.agregarDato("senAJATR", Constantes.NO);
		datos.agregarDato("senAutorizacion", Constantes.NO);
		datos.agregarDato("senReversa", Constantes.NO);
		datos.agregarDato("senSupervisor", peticion.getSenSupervisor());
		datos.agregarDato("senWANG", null);
		datos.agregarDato("senDiaAnterior", Constantes.NO);
		datos.agregarDato("senImpCaja", Constantes.SI);
		datos.agregarDato("senPosteo", new Integer(2));
		datos.agregarDato("valorAnterior", BigDecimal.ZERO);
		datos.agregarDato("valorCompra", BigDecimal.ONE);
		datos.agregarDato("valorEfectivo", peticion.getValorEfectivo());
		datos.agregarDato("valorCheque", BigDecimal.ZERO);
		datos.agregarDato("valorVenta", BigDecimal.ONE);
		datos.agregarDato("numDocumentoTran2", null);
		datos.agregarDato("valorChequesAjenos", BigDecimal.ZERO);
		datos.agregarDato("valorChequesExt", null);
		datos.agregarDato("valorChequesPropios", null);
		datos.agregarDato("descripcionTran", " ");
		datos.agregarDato("codBancoTransf", null);
		datos.agregarDato("numCuentaTransf", "0000000000000");
		datos.agregarDato("codPaisTransf", null);
		datos.agregarDato("senACRM", Constantes.SI);
		//codCliente recuperada en Validar relación transacción-subcausal 
		datos.agregarDato("valorImpuesto", null);
		//tipDocumentoCliente recuperada en Validar relación transacción-subcausal 
		//numDocumentoCliente recuperada en Validar relación transacción-subcausal 
		datos.agregarDato("numDocumentoImp", null);
		//codSubCasual recuperada en  Validar relación colector con documento de pago
		registrarTransaccionAAATR(datos);
	}
	
	
	/**
	 * M&eacutetodo auxiliar Verificar cuenta parametrizada y concepto para hacer nota de credito
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void verificarCuentaParametrizadaYConcepto(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		Integer senEncontrado = 0;
		Integer numDocum = 0;
		Integer codConceptoProducto = 0;
		String numCtaAAMSM = "";
		Integer codProductoCta = datos.obtenerInteger("codProductoCta");
		Integer codOficinaCta = datos.obtenerInteger("codOficinaCta");
		Integer numCuenta = datos.obtenerInteger("numCuenta");
		AdaptadorDeMapa adaptador = null;
		
		
		if(UtileriaDeDatos.isGreater(datos.obtenerInteger("codProductoCta"), 0) && 
	       UtileriaDeDatos.isGreater(datos.obtenerInteger("codOficinaCta"), 0) && 
	       UtileriaDeDatos.isGreater(datos.obtenerInteger("numCuenta"), 0) && 
	       UtileriaDeDatos.isEquals(datos.obtenerInteger("senAbonoCuenta"), Constantes.SI)) {
			try {
				
				numCtaAAMSM = StringUtils.leftPad(codProductoCta.toString(), 3, '0') + StringUtils.leftPad(codOficinaCta.toString(),3,'0') + 
						StringUtils.leftPad(numCuenta.toString(), 6, '0');
				
				logger.debug("Ejecutando sentencia SELECT SFBDB AAMPR, parametro:" + datos.obtenerInteger("codProductoCta"));
				codConceptoProducto = jdbcTemplate.queryForObject(query(SELECT_SFBDB_AAMPR), Integer.class,datos.obtenerInteger("codProductoCta"));

				if(UtileriaDeDatos.isEquals(codConceptoProducto, Constantes.CONCEPTO_AH)) {
					datos.agregarDato("codCausal", Constantes.AH_CAUSAL);
					acreditarCuentaAhorro(datos);
				}
				
				if(UtileriaDeDatos.isEquals(codConceptoProducto, Constantes.CONCEPTO_CC)) {
					try {
						
						Object[] paramsAAMTC = {
								Constantes.GP_CODIGO_TIPO_CORRELATIVO, 
								datos.obtenerInteger("codOficinaCta"), 
								datos.obtenerInteger("codProductoCta")
						};
						logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAMTC, parametros: {}", Arrays.toString(paramsAAMTC));
						Map<String, Object> conceptoCuentaCorriente = jdbcTemplate.queryForMap(query(SELECT_SFBDB_AAMTC), paramsAAMTC);	
						adaptador = UtileriaDeDatos.adaptarMapa(conceptoCuentaCorriente);
						senEncontrado = adaptador.getInteger("senEncontrado");
						datos.agregarDato("numDocum", adaptador.getInteger("numDocum"));
					}catch (EmptyResultDataAccessException e2) {
					}
						if(UtileriaDeDatos.isEquals(senEncontrado, 1)) {
							numDocum = numDocum + 1;
								Object[] paramsAAMTC2 = {
										numDocum,
										Constantes.GP_CODIGO_TIPO_CORRELATIVO, 
										datos.obtenerInteger("codOficinaCta"), 
										datos.obtenerInteger("codProductoCta")
								};
								
								logger.debug("Ejecutando sentencia UPDATE LINC SFBDB AAMTC, parametros: {}", Arrays.toString(paramsAAMTC2));
								jdbcTemplate.queryForMap(query(UPDATE_SFBDB_AAMTC), paramsAAMTC2);	
						}
						datos.agregarDato("codCausal", Constantes.CC_CAUSAL_DEPOSITO);
						acreditarCuentaCorriente(datos);
				}
			}catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20019, "NO EXISTE {}", " CODIGO CONCEPTO PRODUCTO");
			}
		}
		
		datos.agregarDato("numCtaAAMSM", numCtaAAMSM);
	}
	
	
	/**
	 * M&eacutetodo auxiliar para Actualizar registro en tabla ICPGE
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void actualizarRegistroICPEG(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {

		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);

		try {
			if((UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"),Constantes.NO))
					&& 
				(UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_CUOTA_EXACTA) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_MAYOR_CUOTA) || 
				UtileriaDeDatos.isEquals(datos.obtenerInteger("senValidacionRegistro"), Constantes.GC_PAGO_DIFERENTE_CUOTA))
				) 
			{
				String numIdentificacionClienteAux = StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0');
                if (UtileriaDeDatos.isEquals(peticion.getCodColector(), new Integer(503))
                             || UtileriaDeDatos.isEquals(peticion.getCodColector(), new Integer(505))
                             || UtileriaDeDatos.isEquals(peticion.getCodColector(), new Integer(507))) {
                       numIdentificacionClienteAux = " ";
                }
                Object[] paramsICPGE2 = {
                             datos.obtenerInteger("fechaSistemaAMD") ,
                             Constantes.GP_PAGO,
                             Constantes.GP_INGRESO,                                     
                             peticion.getCodColector(),
                             peticion.getCodTipoDocumento(),
                             StringUtils.leftPad(datos.obtenerString("numCredito"), 20, '0'),
//                            StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0'),
                             numIdentificacionClienteAux,
                             datos.obtenerInteger("fechaCarga"),
                             peticion.getGlbDtime()
                };

				logger.debug("Ejecutando sentencia UPDATE SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE2));
				Integer rowcount = getJdbcTemplate().update(query(UPDATE_SFBDB_ICPGE2), paramsICPGE2);

				if(UtileriaDeDatos.isEquals(rowcount, new Integer(0))) {

					Object[] paramsICPGE3 = {
							datos.obtenerInteger("fechaSistemaAMD"),
							Constantes.GP_PAGO,
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							datos.obtenerString("numCredito"),
							datos.obtenerString("numIdentificacionCliente"),
							datos.obtenerInteger("fechaSistemaAMD"),
							peticion.getGlbDtime()
					};
					logger.debug("Ejecutando sentencia UPDATE SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE3));
					ejecutarSentencia(query(UPDATE_SFBDB_ICPGE3), paramsICPGE3);
				}

				if(!UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL) &&
				   !UtileriaDeDatos.isEquals(peticion.getCodColector(), Constantes.GP_COLECTOR_FISDL_INDEMNIZATORIO)) {

					Object[] paramsICPGE4 = {
							StringUtils.leftPad(peticion.getNumDocumentoTran().toString(), 12, '0'),
							Constantes.GP_PAGO,
							peticion.getCodColector(),
							peticion.getCodTipoDocumento(),
							datos.obtenerString("numCredito"),
							peticion.getGlbDtime()
					};
					
					logger.debug("Ejecutando sentencia UPDATE SFBDB ICPGE, parametros: {}", Arrays.toString(paramsICPGE4));
					ejecutarSentencia(query(UPDATE_SFBDB_ICPGE4), paramsICPGE4);
				}
			}

		} catch (EmptyResultDataAccessException e) {
		}
	}
	
	/**
	 * M&eacutetodo auxiliar para Insertar registro en tabla SFBDB_ICATR
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void insertarRegistroICATR(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		Long glbDtime = 0l;                                         
		String numCuentaSfb = "";
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);

		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"), Constantes.NO)) {
			if(UtileriaDeDatos.isEquals(glbDtime.intValue(), 0)) {
				glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME_DIF), Long.class);
			}
			numCuentaSfb = datos.obtenerString("cuentaStr");

			Object[] paramsICATR = {
					glbDtime , peticion.getCodColector() , 
					Constantes.GP_PAGO, 
					peticion.getCodTipoDocumento() , 
					peticion.getCodOficinaTran() , 
					peticion.getCodTerminal() ,
					peticion.getCodCajero() ,
					datos.obtenerInteger("tipoClienteICPGE"),
					datos.obtenerInteger("fechaSistemaAMD") ,
					datos.obtenerInteger("horaSistema"), 
					peticion.getValorMovimiento() , 
					datos.obtenerString("nomCliente") ,
					StringUtils.leftPad(datos.obtenerString("numIdentificacionCliente"), 14, '0'), 
					datos.obtenerInteger("fechaCarga") ,
					datos.obtenerInteger("horaCarga") ,
					datos.obtenerString("numCredito") , 
					peticion.getNumDocumentoTran() , 
					peticion.getNumDocumentoTran(),
					Constantes.NO , 
					datos.obtenerString("codTipoIdentificacion") ,  
					0 , 
					datos.obtenerString("periodoDeclaracion"),  
					datos.obtenerString("numCtaAAMSM") ,  
					peticion.getValorMovimiento(),0,0,0,0,
			};

			logger.debug("Ejecutando sentencia INSERT_SFBDB_ICATR, parametro: {}", Arrays.toString(paramsICATR));
			ejecutarSentencia(query(INSERT_SFBDB_ICATR), paramsICATR);

		}


		String numIdentificacionClienteAAACM = "";
		String nombreClienteAAACM = "";
		String numeroCreditoAAACM = "";

		if(UtileriaDeDatos.isEquals(datos.obtenerInteger("senPlanilla"), Constantes.SI)) {

			UtileriaDeParametros.validarParametro(peticion.getNumLote(), "numLote", TipoValidacion.OBJETO_NULO);
			UtileriaDeParametros.validarParametro(peticion.getNumLote(), "numLote", TipoValidacion.ENTERO_MAYOR_CERO);
			
			Object[] paramsAAACM = {
					datos.obtenerString("codCajero"), 
					peticion.getNumLote(), 
					Constantes.GC_ESTADO_COLECTOR_PENDIENTE
			};
			
			logger.debug("Ejecutando sentencia SELECT LINC SFBDB AAACM, parametros: {}", Arrays.toString(paramsAAACM));
			List<Map<String, Object>> listadoCargasArchivo02 = jdbcTemplate.queryForList(query(SELECT_SFBDB_AAACM), paramsAAACM);
			
			List<Object[]> batch = new ArrayList<Object[]>();
			
			for(Map<String, Object> map : listadoCargasArchivo02) {
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map);
				glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME_DIF), Long.class);
				numIdentificacionClienteAAACM = adaptador.getString("numRegArchivoCargado").substring(11, 25);
				nombreClienteAAACM = adaptador.getString("numRegArchivoCargado").substring(75, 120);
				numeroCreditoAAACM = adaptador.getString("numRegArchivoCargado").substring(25, 45);

				Object[] paramsICATR = {
						glbDtime , 
						datos.obtenerInteger("codColector") , 
						Constantes.GP_PAGO , 
						datos.obtenerInteger("codTipoDocumento") , 
						datos.obtenerInteger("codOficinaTran") , 
						datos.obtenerInteger("codTerminalTran") ,
						datos.obtenerString("codCajero")  ,
						datos.obtenerInteger("tipoClienteICPGE"),
						datos.obtenerInteger("fechaSistemaAMD") ,
						datos.obtenerInteger("horaSistema"),
						datos.obtenerBigDecimal("valorMovimiento") , 
						nombreClienteAAACM ,
						numIdentificacionClienteAAACM , 
						datos.obtenerInteger("fechaCarga") ,
						datos.obtenerInteger("horaCarga") ,
						numeroCreditoAAACM ,
						datos.obtenerInteger("numDocumentoTran") ,
						datos.obtenerInteger("numDocumentoTran"),
						Constantes.NO , 
						datos.obtenerString("codTipoIdentificacion") ,  
						datos.obtenerInteger("fechaVencimiento") , 
						datos.obtenerString("periodoDeclaracion"),  
						numCuentaSfb ,  
						0
				};
				
				batch.add(paramsICATR);
//				logger.debug("Ejecutando sentencia INSERT_SFBDB_ICATR, parametro: {}", Arrays.toString(paramsICATR));
//				ejecutarSentencia(query(INSERT_SFBDB_ICATR), paramsICATR);
			}
			
			if (UtileriaDeDatos.isGreaterThanZero(batch.size())) {
				jdbcTemplate.batchUpdate(query(INSERT_SFBDB_ICATR2), batch);//
			}
			
		}
	}
	
	
	/**
	 * M&eacutetodo auxiliar para Asignación de variables de salida
	 * @param datos
	 * @throws ServicioException
	 * @throws TipoDatoException 
	 * @throws ParseException 
	 */
	private void asignacionVariablesSalida(DatosOperacion datos) throws ServicioException, TipoDatoException, ParseException {
		
		Cliente cliente = datos.obtenerObjeto("cliente", Cliente.class);
		String nomCliente = datos.obtenerString("nomCliente");
		PagaduriaGenericoPeticion peticion = datos.obtenerObjeto("peticion", PagaduriaGenericoPeticion.class);
		
		String impFISDL = "";
		if(!UtileriaDeDatos.isNull(nomCliente)) {
			impFISDL = "Nomb: " + nomCliente + impFISDL;
		}
		
		if(UtileriaDeDatos.isBlank(nomCliente)) {
			nomCliente = cliente.getNombreCompletoCliente();
		}
		
		String periodoDeclaracion = datos.obtenerString("periodoDeclaracion");
		Integer fechaTransaccion = datos.obtenerInteger("fechaSistema");
		String nomAgencia = datos.obtenerString("nomOficinaTran");
		String dui = cliente.getDuiCliente();
		
		datos.agregarDato("periodoPago", periodoDeclaracion);
		datos.agregarDato("fechaTransaccion", fechaTransaccion);
		datos.agregarDato("nomAgencia", nomAgencia);
		datos.agregarDato("dui", dui);
		datos.agregarDato("nomCliente", nomCliente);
		datos.agregarDato("tipoDocumento", cliente.getTipDocumentoCliente());
		datos.agregarDato("glbDtime", peticion.getGlbDtime());
		
	}
	

}
