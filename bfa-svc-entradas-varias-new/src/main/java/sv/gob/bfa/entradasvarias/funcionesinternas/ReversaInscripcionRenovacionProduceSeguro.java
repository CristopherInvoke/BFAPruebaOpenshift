package sv.gob.bfa.entradasvarias.funcionesinternas;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import sv.gob.bfa.core.fs.FSActualizarPerfilesTransaccionAAATR;
import sv.gob.bfa.core.fs.FSBase;
import sv.gob.bfa.core.model.Peticion;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;

public class ReversaInscripcionRenovacionProduceSeguro extends FSBase{

	private static final String NOM_COD_FNC = "Reversa inscripcion renovacion produce seguro: ";
	
	private static final String COD_FI = "FIRIRPS: ";
	
	private static final String SELECT_LINC_SFBDB_PPRGP = 
			"SELECT PCO_ESTAD AS codEstadoGasto," + 
			"       GLB_DTIME AS glbDtimePPRGP " + 
			"  FROM LINC.SFBDB_PPRGP@DBLINK@" + 
			"    WHERE PCU_OFICI = ?" + 
			"      AND PCU_PRODU = ?" + 
			"      AND PCUNUMCUE = ?" + 
			"      AND PCO_GASTO = ?" + 
			"      AND ROWNUM = 1" + 
			" ORDER BY PFEINIAPL DESC"
			;
	
	private static final String UPDATE_LINC_SFBDB_PPRGP = 
			"UPDATE LINC.SFBDB_PPRGP@DBLINK@" + 
			"	SET PCO_ESTAD = ?," + 
			"		PSEGASUTI = ?" + 
			"	WHERE GLB_DTIME = ?";
	
	private static final String SELECT_LINC_SFBDB_AAATR = 
			"SELECT GLB_DTIME as glbDtimeAAATR" + 
			"	FROM LINC.SFBDB_AAATR@DBLINK@" + 
			"	WHERE	TFETRAREL = ?" + 
			"		AND DCO_OFICI = ?" + 
			"		AND DCO_TERMI = ?" + 
			"		AND TNU_TRANS = ?" + 
			"		AND ACU_PRODU = ?" + 
			"		AND ACU_OFICI = ?" + 
			"		AND ACUNUMCUE = ?" + 
			"		AND TNUDOCTRA = ?" + 
			"		AND TSE_REVER != ?";
	
	private static final String UPDATE_LINC_SFBDB_AAATR = 
			"UPDATE LINC.SFBDB_AAATR@DBLINK@" + 
			"   SET TSE_REVER = ?" + 
			" WHERE GLB_DTIME = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_PPANB = 
			"SELECT MAX(glb_dtime) AS glbDtimePPANB" + 
			"	FROM LINC.SFBDB_PPANB@DBLINK@" + 
			"	WHERE	PCU_OFICI = ?" + 
			"		AND PCU_PRODU = ?" + 
			"		AND PCUNUMCUE = ?" + 
			"		AND PFE_EVENT = ?" + 
			"		AND DCO_ISPEC = ?"
			;
	
	private static final String DELETE_LINC_SFBDB_PPANB = 
			"DELETE FROM LINC.SFBDB_PPANB@DBLINK@" + 
			"	WHERE GLB_DTIME = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_PPMTP = 
			"SELECT glb_dtime AS glbDtimePPMTP" + 
			"	FROM LINC.SFBDB_PPMTP@DBLINK@" + 
			"	WHERE	PCU_OFICI = ?" + 
			"		AND PCU_PRODU = ?" + 
			"		AND PCUNUMCUE = ?" + 
			"		AND PFE_EVENT = ?" + 
			"		AND DCO_ISPEC = ?" + 
			"		AND ACO_CAUSA = ?" + 
			"		AND ROWNUM    = ?" + 
			"		ORDER BY 1"
			;
	
	private static final String DELETE_LINC_SFBDB_PPMTP = 
			"DELETE FROM LINC.SFBDB_PPMTP@DBLINK@" + 
			"	WHERE GLB_DTIME = ?"
			;
	
	private static final String SELECT_LINC_SFBDB_IEAPS = 
			"SELECT GLB_DTIME AS glbDtimeIEAPS," + 
			"		ICO_ESTAD AS codEstado" + 
			"	FROM LINC.SFBDB_IEAPS@DBLINK@" + 
			"	WHERE	PCU_OFICI = ?" + 
			"   	AND PCU_PRODU = ?" + 
			"   	AND PCUNUMCUE = ?" + 
			"		AND ICO_ESTAD != ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_IEAPS = 
			"UPDATE LINC.SFBDB_IEAPS@DBLINK@" + 
			"   SET ICO_ESTAD = ?," + 
			"       ICO_ERROR = ?" + 
			" WHERE GLB_DTIME = ?"
			;
	
	private static final String UPDATE_LINC_SFBDB_IEAPS_O = 
			"UPDATE LINC.SFBDB_IEAPS@DBLINK@" + 
			"   SET ICO_ESTAD = ?" + 
			" WHERE GLB_DTIME = ?"
			;
	
	public ReversaInscripcionRenovacionProduceSeguro(JdbcTemplate jdbcTemplate, String dbLink) {
		super(jdbcTemplate, dbLink);
	}

	public void reversarInscripcionRenovacionProduceSeguro(DatosOperacion datos) throws TipoDatoException, ServicioException {
		
		//Parametros de entrada
		Integer codProducto = datos.obtenerInteger("codProducto");
		Integer codOficina = datos.obtenerInteger("codOficina");
		Integer numCuenta = datos.obtenerInteger("numCuenta");
		Integer fechaSistemaAMD = datos.obtenerInteger("fechaSistemaAMD");
		Integer codTerminal = datos.obtenerInteger("codTerminal");
		Integer numReversa = datos.obtenerInteger("numReversa");
		Integer numDocumentoTran = datos.obtenerInteger("numDocumentoTran");
		String codPantalla = datos.obtenerString("codPantalla");
		Integer codCausal = datos.obtenerInteger("codCausal");
		Integer codOficinaTran = datos.obtenerInteger("codOficinaTran");
		Long glbDtimeAAATR = null;
		
		UtileriaDeParametros.validarParametro(codProducto, COD_FI + "codProducto", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codOficina, COD_FI + "codOficina", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(numCuenta, COD_FI + "numCuenta", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(fechaSistemaAMD, COD_FI + "fechaSistemaAMD", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codTerminal, COD_FI + "codTerminal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(numReversa, COD_FI + "numReversa", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(numDocumentoTran, COD_FI + "numDocumentoTran", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codPantalla, COD_FI + "codPantalla", TipoValidacion.CADENA_VACIA);
		UtileriaDeParametros.validarParametro(codCausal, COD_FI + "codCausal", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codOficinaTran, COD_FI + "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);
		
		Object[] paramsPPRGP = {
				codOficina,
				codProducto,
				numCuenta,
				Constantes.PS_COD_GASTO_SUBSIDIO_BFA,
				fechaSistemaAMD
		};
		
		//2. 
		logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB PPRGP, parametros: {}", Arrays.toString(paramsPPRGP));
		List<Map<String, Object>> gastosRelacionados = jdbcTemplate.queryForList(query(SELECT_LINC_SFBDB_PPRGP), paramsPPRGP);
		Map<String, Object> map = gastosRelacionados.get(0);
		AdaptadorDeMapa gasto = UtileriaDeDatos.adaptarMapa(map);
		
		if (UtileriaDeDatos.isEquals(gasto.getInteger("codEstadoGasto"), Constantes.PS_COD_ESTADO_GASTO_CANCELADO)) {
			
			Object[] paramsPPRGPUpdate = {
					Constantes.PS_COD_ESTADO_GASTO_INGRESADO,
					Constantes.NO,
					gasto.getLong("glbDtimePPRGP")
			};
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia UPDATE LINC SFBDB PPRGP, parametros: {}");
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_PPRGP), paramsPPRGPUpdate, "UPDATE_LINC_SFBDB_PPRGP");
		}
		
		//3.
		Object[] paramsAAATR = {
			datos.obtenerInteger("fechaRelativa"),
			codOficinaTran,
			codTerminal,
			numReversa,
			codProducto,
			codOficina,
			numCuenta,
			numDocumentoTran,
			Constantes.SI
		};
		
		
		try {
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB AAATR, parametros: {}", Arrays.toString(paramsAAATR));
			glbDtimeAAATR = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_AAATR), Long.class, paramsAAATR);
		} catch (EmptyResultDataAccessException e) {
		}
		

		Object[] paramsAAATRUpdate = {
				Constantes.SI,
				glbDtimeAAATR
		};
		logger.debug(NOM_COD_FNC + "Ejecutando sentencia UPDATE LINC SFBDB AAATR, parametros: {}", Arrays.toString(paramsAAATRUpdate));
		ejecutarSentencia(query(UPDATE_LINC_SFBDB_AAATR), paramsAAATRUpdate, "UPDATE_LINC_SFBDB_AAATR");

		//4. 
		FSActualizarPerfilesTransaccionAAATR fnc = new FSActualizarPerfilesTransaccionAAATR(jdbcTemplate, dbLink);
		datos.agregarDato("glbDtime", glbDtimeAAATR);
		fnc.actualizarPerfilesTransaccionAAATR(datos);
		
		if(!UtileriaDeDatos.isNull(glbDtimeAAATR)) {

			Object[] paramsPPANB = {
					codOficina,
					codProducto,
					numCuenta,
					fechaSistemaAMD,
					codPantalla
			};

			logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB PPANB, parametros: {}", Arrays.toString(paramsPPANB));
			Long glbDtimePPANB = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_PPANB), Long.class, paramsPPANB);

			Object[] paramsPPANBDelete = {
					glbDtimePPANB
			};

			logger.debug(NOM_COD_FNC + "Ejecutando sentencia DELETE LINC SFBDB PPANB, parametros: {}", Arrays.toString(paramsPPANBDelete));
			ejecutarSentencia(query(DELETE_LINC_SFBDB_PPANB), paramsPPANBDelete, "DELETE_LINC_SFBDB_PPANB");

			Object[] paramsPPMTP = {
					codOficina,
					codProducto,
					numCuenta,
					fechaSistemaAMD,
					codPantalla,
					codCausal,
					new Integer(1)
			};

			logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB PPMTP, parametros: {}", Arrays.toString(paramsPPMTP));
			Long glbDtimePPMTP = jdbcTemplate.queryForObject(query(SELECT_LINC_SFBDB_PPMTP), Long.class, paramsPPMTP);

			Object[] paramsPPMTPDelete = {
					glbDtimePPMTP
			};

			logger.debug(NOM_COD_FNC + "Ejecutando sentencia DELETE LINC SFBDB PPMTP, parametros: {}", Arrays.toString(paramsPPMTPDelete));
			ejecutarSentencia(query(DELETE_LINC_SFBDB_PPMTP), paramsPPMTPDelete, "DELETE_LINC_SFBDB_PPMTP");
		}
		
		//5.
		
		Object[] paramsIEAPS = {
			codOficina,
			codProducto,
			numCuenta,
			Constantes.PS_REVERSADO_BFA
		};
		
		logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB IEAPS, parametros: {}", Arrays.toString(paramsIEAPS));
		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(query(SELECT_LINC_SFBDB_IEAPS), paramsIEAPS);
		
		AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(queryForMap);
		Integer codEstado = adaptador.getInteger("codEstado");
		
		if (UtileriaDeDatos.isEquals(codEstado, Constantes.PS_REGISTRADO_BFA)) {
			Object[] paramsIEAPSUpdate = {
				Constantes.PS_REVERSADO_BFA,
				new Integer(51),
				adaptador.getLong("glbDtimeIEAPS")
			};
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia UPDATE LINC SFBDB IEAPS, parametros: {}", Arrays.toString(paramsIEAPSUpdate));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_IEAPS), paramsIEAPSUpdate);
		}
		
		if (UtileriaDeDatos.isEquals(codEstado, Constantes.PS_ENVIADO_BFA)) {
			Object[] paramsIEAPSUpdate = {
				Constantes.PS_REVERSADO_BFA,
				new Integer(52),
				adaptador.getLong("glbDtimeIEAPS")
			};
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia UPDATE LINC SFBDB IEAPS, parametros: {}", Arrays.toString(paramsIEAPSUpdate));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_IEAPS), paramsIEAPSUpdate);
		}
		
		if (UtileriaDeDatos.isEquals(codEstado, Constantes.PS_RECIBIDO_BFA_SIN_ERROR)) {
			Object[] paramsIEAPSUpdate = {
				Constantes.PS_REVERSADO_BFA,
				adaptador.getLong("glbDtimeIEAPS")
			};
			logger.debug(NOM_COD_FNC + "Ejecutando sentencia UPDATE LINC SFBDB IEAPS O, parametros: {}", Arrays.toString(paramsIEAPSUpdate));
			ejecutarSentencia(query(UPDATE_LINC_SFBDB_IEAPS_O), paramsIEAPSUpdate);
		}
		
	}
	
}
