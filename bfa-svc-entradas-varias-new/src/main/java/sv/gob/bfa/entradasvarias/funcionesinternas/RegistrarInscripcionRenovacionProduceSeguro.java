package sv.gob.bfa.entradasvarias.funcionesinternas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import sv.gob.bfa.core.fs.FSBase;
import sv.gob.bfa.core.model.Cliente;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;

public class RegistrarInscripcionRenovacionProduceSeguro extends FSBase{
	
	private static final String NOM_COD_FNC = "Registro inscripcion renovacion produce seguro: ";
	
	private static final String SELECT_SFBDB_PPASO = "select pnu_renov as numRenovacionPPASO," + 
			"	aco_usuar as codUsuarioPPASO," + 
			"	sco_ident as codClientePPASO," + 
			"	snu_direc as codDireccionPPASO," + 
			"	pco_desti as codDestinoPPASO," + 
			"	pmo_credi as montoCreditoPPASO," + 
			"	pfedocleg as fechaDocumentoLegal," + 
			"	pfe_liqui as fechaDesembolsoPPASO," + 
			"	pfe_pagar as fechaVencimientoPPASO" + 
			"	from linc.sfbdb_ppaso@DBLINK@" + 
			"	where pcu_ofici = ?" + 
			"	and pcu_produ = ?" + 
			"	and pcunumcue = ?";
	
	private static final String SELECT_IVA = "select bpo_iva as IVA" + 
			"	from linc.sfbdb_ivmpa@DBLINK@"; 
	
	private static final String SELECT_SFBDB_PPRSP = "select pcotipseg as codTipoSeguro," + 
			"	pfevenpol as fechaVencePoliza," + 
			"	pmo_segur as montoAsegurado," + 
			"	pva_prima as valorPrimaCompletaSeguro" + 
			"	from linc.sfbdb_pprsp@DBLINK@" + 
			"	where pcu_ofici = ?" + 
			"	and pcu_produ = ?" + 
			"	and pcunumcue = ?" + 
			"	and pcoempseg = ?";
	
	private static final String SELECT_DIR_CLIENTE = "select sno_direc|| ' ' ||sno_dire2 AS direccionCliente," + 
			"	scociate1 AS codCompaniaTelefonica," + 
			"	snu_tele1 AS numTelefono," + 
			"	snulatitud as latitud," + 
			"	snulongitu as longitud" + 
			"	from linc.sfbdb_bsmdi@DBLINK@" + 
			"	where sco_ident = ?" + 
			"	and snu_direc = ?";
	
	private static final String SELECT_DIR_OTRO_CLIENTE = "select sno_direc || ' ' ||sno_dire2 AS otraDireccion," + 
			"	scozongeo AS codZonaGeografica" + 
			"	from linc.sfbdb_bsmdi@DBLINK@" + 
			"	where sco_ident = ?" + 
			"	and snu_direc = ?";
	
	private static final String SELECT_SFBDB_BSMBS = "select sco_leer AS codLeeEscribe," + 
			"	sconivedu AS codNivelEstudio," + 
			"	ssecabfam AS senCabezaFamilia," + 
			"	snu_hijos AS numDependientes," + 
			"	sno_benef as nombreBeneficiario" + 
			"	from linc.sfbdb_bsmbs@DBLINK@" + 
			"	where sco_ident = ?" + 
			"   and pcu_ofici = ?" + 
			"   and pcu_produ = ?" + 
			"	and pcunumcue = ?";
	
	private static final String  SELECT_COUNT_DAMOF = "select count(glb_dtime) contadorAgencia" + 
			"  from linc.sfbdb_damof@DBLINK@" + 
			"   where dco_ofici = ?";
	
	private static final String SELECT_GLBDTIME = "SELECT MADMIN.GENERATE_GLBDTIME_DIF as glbDtime FROM DUAL";
	
	private static final String INSERT_SFBDB_IEAPS = "INSERT INTO LINC.SFBDB_IEAPS@DBLINK@ (" + 
			"GLB_DTIME, ICO_ESTAD," + 
			"PCU_OFICI, PCU_PRODU," + 
			"PCUNUMCUE, PFEFINPOL," + 
			"PFEINIPOL, ACOASECRE," + 
			"AFE_INCLU, AHO_INCLU," + 
			"DCO_OFICI, DCO_USUAR," + 
			"ICO_ERROR, ICOTIPOPE," + 
			"INU_ENVIO, PCNPERCAR," + 
			"PCO_DESTI, PCOFORPAG," + 
			"PCONIVEDU, PCUDIGVER," + 
			"PMO_SEGUR, PNU_CERTI," + 
			"PNUPOLIZA, PSECABFAM," + 
			"PSEESCLEE, PVA_IMPUES," + 
			"PVA_PRIMA, PVAPAGTOT," + 
			"SCO_IDENT, SCO_OCUPA," + 
			"SCOCIATEL, SCOGENERO," + 
			"SCOZONGEO, SFE_NACIM," + 
			"SNOAPELLI, SNODIRCLI," + 
			"SNODIRPRO, SNONOMBRE," + 
			"SNU_NIT, SNU_TELEF," + 
			"SNUDUICLI, SNULATITUD," + 
			"SNULONGITU, SSEOTRSEG," + 
			"TFE_ENVIO, TFE_RECEP," + 
			"THO_ENVIO, THO_RECEP," + 
			"ACOUSUINC, ACOUSUMOD," + 
			"AFE_MODIF, AFEENVMOD," + 
			"AHO_MODIF, AHOENVMOD," + 
			"SNO_BENEF, DNO_OFICI," + 
			"PFE_DESEM, PFE_VENCI," + 
			"PFEOTORGA, PMO_CREDI" + 
			")" + 
			"Values(" + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," +
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," +
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," +
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," +
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," +
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?," + 
			"?, ?)";

	public RegistrarInscripcionRenovacionProduceSeguro(JdbcTemplate jdbcTemplate, String dbLink) {
		super(jdbcTemplate, dbLink);
	}
	
	
	public void registrarInscripcionRenovacionSeguro(DatosOperacion datos) throws TipoDatoException, ParseException, ServicioException{
		
		//Parametros de entrada
		Integer codProducto = datos.obtenerInteger("codProducto");
		Integer codOficina = datos.obtenerInteger("codOficina");
		Integer numCuenta = datos.obtenerInteger("numCuenta");
		Integer digitoVerificador = datos.obtenerInteger("digitoVerificador");
		String codCliente = datos.obtenerString("codCliente");
		Integer fechaSistemaAMD = datos.obtenerInteger("fechaSistemaAMD");
		Integer horaSistema = datos.obtenerInteger("horaSistema");
		Integer codOficinaTran = datos.obtenerInteger("codOficinaTran");
		String nomOficinaTran = datos.obtenerString("nomOficinaTran");
		String codCajero = datos.obtenerString("codCajero");
		String codTipoOperacion = datos.obtenerString("codTipoOperacion");

		logger.debug("Validacion de campos requeridos para la operaci{on");
		
		
		UtileriaDeParametros.validarParametro(codProducto,  NOM_COD_FNC + "codProducto", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codOficina,  NOM_COD_FNC + "codOficina", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(numCuenta,  NOM_COD_FNC + "numCuenta", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(digitoVerificador,  NOM_COD_FNC + "digitoVerificador", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(codCliente,  NOM_COD_FNC + "codCliente", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(fechaSistemaAMD,  NOM_COD_FNC + "fechaSistemaAMD", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(horaSistema,  NOM_COD_FNC + "horaSistema", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codOficinaTran,  NOM_COD_FNC + "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(nomOficinaTran,  NOM_COD_FNC + "nomOficinaTran", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(codCajero,  NOM_COD_FNC + "codCajero", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(codTipoOperacion,  NOM_COD_FNC + "codTipoOperacion", TipoValidacion.CADENA_VACIA);

		logger.debug("Inicializacion de variables a utilizar y busqueda de prestamo en maestro de solicitudes");
		
		Integer correlativoRegistroArchivo = Constantes.REGISTRO_ENVIO;
		Integer numRenovacion = 0;
		Integer senPoseeOtroSeguro = Constantes.NO;
		Integer codFormaPago = Constantes.ANUAL;
		BigDecimal porcentajeIVA = BigDecimal.ZERO;
		Integer senGrabaRegistroIEAPS = Constantes.NO;
		String codAsesorCreditos = "";
		
		Object[] paramsPPASO = {
				codOficina,
				codProducto,
				numCuenta
		};
		
		logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB PPASO, parametros: {}", Arrays.toString(paramsPPASO));
		Map<String, Object> map_PPASO = jdbcTemplate.queryForMap(query(SELECT_SFBDB_PPASO), paramsPPASO);
	
		AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map_PPASO);
		Integer numRenovacionPPASO = adaptador.getInteger("numRenovacionPPASO");
		String codUsuarioPPASO = adaptador.getString("codUsuarioPPASO");
		String codClientePPASO = adaptador.getString("codClientePPASO");
		Integer codDireccionPPASO = adaptador.getInteger("codDireccionPPASO");
		Integer codDestinoPPASO = adaptador.getInteger("codDestinoPPASO");
		BigDecimal montoCreditoPPASO = adaptador.getBigDecimal("montoCreditoPPASO");
		Integer fechaDocumentoLegal = adaptador.getInteger("fechaDocumentoLegal");
		Integer fechaDesembolsoPPASO = adaptador.getInteger("fechaDesembolsoPPASO");
		Integer fechaVencimientoPPASO = adaptador.getInteger("fechaVencimientoPPASO");
		
		
		logger.debug("Se evalúan datos de la solicitud");
		
		if(UtileriaDeDatos.isGreater(numRenovacionPPASO, numRenovacion)){
			numRenovacion = numRenovacionPPASO;
		}
		
		if(UtileriaDeDatos.isEquals(numRenovacionPPASO, numRenovacion)){
			codAsesorCreditos = codUsuarioPPASO;
		}
		
		logger.debug("Accediendo a la tabla de mantenimiento de parámetros para obtener el porcentaje de IVA");
		
		BigDecimal iva = jdbcTemplate.queryForObject(query(SELECT_IVA), BigDecimal.class);
		porcentajeIVA = iva;
		porcentajeIVA = porcentajeIVA.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
		porcentajeIVA = porcentajeIVA.add(new BigDecimal(1));
		
		
		logger.debug("Accediendo a la tabla de relación Seguro-Préstamo");
		List<Map<String, Object>> recuperaGastosFacturadosList = recuperaGastosFacturados(datos);
		Integer contador = 0;
		Integer fechaVencePolizaAux = 0;
		BigDecimal valorPrimaSeguro = BigDecimal.ZERO;
		BigDecimal valorImpuesto = BigDecimal.ZERO;
		BigDecimal montoAsegurado = BigDecimal.ZERO;
		BigDecimal valorPrimaCompletaSeguro = BigDecimal.ZERO;
		for (Map<String, Object> map : recuperaGastosFacturadosList) {
			adaptador = UtileriaDeDatos.adaptarMapa(map);
			fechaVencePolizaAux = adaptador.getInteger("fechaVencePoliza");
			montoAsegurado = adaptador.getBigDecimal("montoAsegurado");
			valorPrimaCompletaSeguro = adaptador.getBigDecimal("valorPrimaCompletaSeguro");
			
			if(!UtileriaDeDatos.isEquals(adaptador.getInteger("codTipoSeguro"), Constantes.PS_TIPO_SEGURO_PRODUCE) &&
					UtileriaDeDatos.isGreater(adaptador.getInteger("fechaVencePoliza"), datos.obtenerInteger("fechaSistemaAMD"))) {
				logger.debug(" Si el tipo de seguro es distinto a Produce Seguro"); 
				senPoseeOtroSeguro = Constantes.SI;
			}
			
			if(UtileriaDeDatos.isEquals(adaptador.getInteger("codTipoSeguro"), Constantes.PS_TIPO_SEGURO_PRODUCE) &&
					UtileriaDeDatos.isEquals(contador, new Integer(0))) {
				logger.debug(" Si es Produce Seguro se relacionan calculos"); 
				
				valorPrimaSeguro = adaptador.getBigDecimal("valorPrimaCompletaSeguro");
				valorPrimaSeguro = valorPrimaSeguro.divide(porcentajeIVA, 2, RoundingMode.HALF_UP);
				
				valorImpuesto = adaptador.getBigDecimal("valorPrimaCompletaSeguro");
				valorImpuesto = valorImpuesto.subtract(valorPrimaSeguro);
				codFormaPago  = Constantes.PRORRATEADO;
				senGrabaRegistroIEAPS = Constantes.SI;
				contador = 1;
			}
		}
		
		Cliente cliente = null;
		if(UtileriaDeDatos.isEquals(senGrabaRegistroIEAPS, Constantes.SI)) {
			logger.debug("Recuperando Datos Cliente");
			datos.agregarDato("codCliente", codCliente);
			cliente = datos.obtenerObjeto("cliente", Cliente.class);
			
			logger.debug("Recuperando datos de la direccion del cliente");
			
			String direccionCliente = "Cliente sin dirección principal";
					
			Object[] paramsBSMDI = {
					codCliente,
					new Integer(1)
			};
			
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB BSMDI, parametros: {}", Arrays.toString(paramsBSMDI));
			Map<String, Object>  map_direccionCliente= jdbcTemplate.queryForMap(query(SELECT_DIR_CLIENTE), paramsBSMDI);
			
			adaptador = UtileriaDeDatos.adaptarMapa(map_direccionCliente);
			direccionCliente = adaptador.getString("direccionCliente");
			Integer codCompaniaTelefonica = adaptador.getInteger("codCompaniaTelefonica");
			String numTelefono = adaptador.getString("numTelefono");
			BigDecimal latitud = adaptador.getBigDecimal("latitud");
			BigDecimal longitud = adaptador.getBigDecimal("longitud");
			
			if (UtileriaDeDatos.isNull(latitud)) {
				latitud = BigDecimal.ZERO;
			}
			if (UtileriaDeDatos.isNull(longitud)) {
				longitud = BigDecimal.ZERO;
			}
			
			logger.debug("Recuperando datos de otra direccion cliente");
			String otraDireccion = "Cliente sin dirección principal";
			
			Object[] paramsBSMDI_ = {
					codCliente,
					codDireccionPPASO
			};
			
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB BSMDI, parametros: {}", Arrays.toString(paramsBSMDI_));
			Map<String, Object>  map_otradireccion = jdbcTemplate.queryForMap(query(SELECT_DIR_OTRO_CLIENTE), paramsBSMDI_);
		
			adaptador = UtileriaDeDatos.adaptarMapa(map_otradireccion);
			otraDireccion = adaptador.getString("otraDireccion");
			Integer codZonaGeografica = adaptador.getInteger("codZonaGeografica");
			
			logger.debug("Recuperando datos de nivel de estudio y número de dependientes del cliente");
			
			Object[] paramsBSMBS_ = {
					codCliente,
					codOficina,
					codProducto,
					numCuenta
			};
			
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB BSMBS, parametros: {}", Arrays.toString(paramsBSMBS_));
			Map<String, Object>  map_beneficiarioSeguro = jdbcTemplate.queryForMap(query(SELECT_SFBDB_BSMBS), paramsBSMBS_);
			
			adaptador = UtileriaDeDatos.adaptarMapa(map_beneficiarioSeguro);
			Integer codLeeEscribe = adaptador.getInteger("codLeeEscribe");
			Integer codNivelEstudio = adaptador.getInteger("codNivelEstudio");
			Integer senCabezaFamilia = adaptador.getInteger("senCabezaFamilia");
			Integer numDependientes = adaptador.getInteger("numDependientes");
			String nombreBeneficiario = adaptador.getString("nombreBeneficiario");
			
			Integer contadorAgencia = jdbcTemplate.queryForObject(query(SELECT_COUNT_DAMOF), Integer.class, codOficina);
			
			if(UtileriaDeDatos.isEquals(contadorAgencia, new Integer(0))) {
				throw new ServicioException(20019, "NO EXISTE {} " , "AGENCIA "+ codOficina + " NO ENCONTRADA EN SFBDB_DAMOF") ;
			}
			
			logger.debug("Generando llave glbDtime");
			Long glbDtime = jdbcTemplate.queryForObject(query(SELECT_GLBDTIME), Long.class);
			
			logger.debug("Insertando en tabla SFBDB_IEAPS");
			Object[] paramsIEAPS = {
					glbDtime,Constantes.PS_REGISTRADO_BFA,
					codOficina,codProducto,
					numCuenta,fechaVencePolizaAux,
					fechaSistemaAMD,codAsesorCreditos,
					fechaSistemaAMD,horaSistema,
					codOficinaTran,codCajero,
					new Integer(0), codTipoOperacion,
					correlativoRegistroArchivo, numDependientes,
					codDestinoPPASO, codFormaPago,
					codNivelEstudio,digitoVerificador,
					montoAsegurado, new Integer(0),
					new Integer(0), senCabezaFamilia,
					codLeeEscribe, valorImpuesto,
					valorPrimaSeguro, valorPrimaCompletaSeguro,
					codClientePPASO, cliente.getCodOcupacion(),
					codCompaniaTelefonica, cliente.getCodGenero(),
					codZonaGeografica, cliente.getFechaNacimiento(),
					cliente.getApellidoModificadoCliente(),direccionCliente,
					otraDireccion, cliente.getNombresCliente(),
					cliente.getNitClienteInsertar(),numTelefono,
					cliente.getDuiClienteInsertar(),latitud,
					longitud,senPoseeOtroSeguro,
					new Integer(0), new Integer(0),
					new Integer(0), new Integer(0),
					" ", " ",
					new Integer(0), new Integer(0),
					new Integer(0), new Integer(0),
					nombreBeneficiario,nomOficinaTran,
					fechaDesembolsoPPASO, fechaVencimientoPPASO,
					fechaDocumentoLegal, montoCreditoPPASO
			};
			
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia INSERT LINC SFBDB IEAPS, parametros: " + Arrays.toString(paramsIEAPS));
			ejecutarSentencia(query(INSERT_SFBDB_IEAPS), paramsIEAPS, "INSERT_SFBDB_IEAPS");
			
		}
		
	}
	
	
	private List<Map<String, Object>> recuperaGastosFacturados(DatosOperacion datos) throws TipoDatoException{

		Object[] paramsPPRSP = {
				datos.obtenerInteger("codOficina"),
				datos.obtenerInteger("codProducto"),
				datos.obtenerInteger("numCuenta"),
				Constantes.PS_ASEGURADORA_SEGURO_FUTURO
		};
		
		logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB PPRSP, parametros: {}", Arrays.toString(paramsPPRSP));
		List<Map<String, Object>> datosSeguro = jdbcTemplate.queryForList(query(SELECT_SFBDB_PPRSP), paramsPPRSP);
		return datosSeguro;
	}
	

}
