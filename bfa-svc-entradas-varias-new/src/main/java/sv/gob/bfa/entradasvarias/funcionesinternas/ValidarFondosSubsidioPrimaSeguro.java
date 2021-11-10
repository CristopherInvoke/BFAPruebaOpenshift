package sv.gob.bfa.entradasvarias.funcionesinternas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import sv.gob.bfa.core.fs.FSBase;
import sv.gob.bfa.core.svc.Constantes;
import sv.gob.bfa.core.svc.DatosOperacion;
import sv.gob.bfa.core.svc.ServicioException;
import sv.gob.bfa.core.svc.TipoDatoException;
import sv.gob.bfa.core.util.AdaptadorDeMapa;
import sv.gob.bfa.core.util.UtileriaDeDatos;
import sv.gob.bfa.core.util.UtileriaDeParametros;
import sv.gob.bfa.core.util.UtileriaDeParametros.TipoValidacion;

public class ValidarFondosSubsidioPrimaSeguro extends FSBase{
	
	private static final String NOM_COD_FNC = "Validar Fondos Subsidio Prima Seguro: ";

	private static final String SELECT_SFBDB_PPMFS = "select aco_gasto as codGasto," + 
			"	ppo_banco as porcentajeCoberturaBFA," + 
			"	ppo_clien as porcentajeCoberturaCliente," + 
			"	aco_tabla as codTabla" + 
			"	from linc.sfbdb_ppmfs@DBLINK@" + 
			"	where pcoconesp = ?" + 
			"   and pcopagseg = ?" ;
	
	private static final String SELECT_SFBDB_BSMTG = "SELECT ano_larga as nomDestino" + 
			"	FROM linc.sfbdb_bsmtg@DBLINK@" + 
			"	WHERE aco_tabla = ?" + 
			"   AND aco_codig = (LPAD(?, 5, 0))";
	
	private static final String SELECT_SFBDB_PPMCE = "select pmomaxcon as montoAsignado," + 
			"   pmo_utili as montoUtilizadoRecuperado," + 
			"   pcnpreben as cantidadPrestamosBenef," + 
			"   pmomaxcon as fondosDisponiblesRecuperado" + 
			"   from linc.sfbdb_ppmce@DBLINK@" + 
			"   where aco_conce = ?" + 
			"	and pcoconesp = ?" + 
			"	and dco_ofici = ?" + 
			"	and pco_desti = ?";
	
	public ValidarFondosSubsidioPrimaSeguro(JdbcTemplate jdbcTemplate, String dbLink) {
		super(jdbcTemplate, dbLink);
	}
	
	
	public void validarFondosSubsidioPrimaSeguro (DatosOperacion datos) throws TipoDatoException, ParseException, ServicioException {

		//Parametros de entrada
		Integer plazoMaximoDias = datos.obtenerInteger("plazoMaximoDias");
		Integer diasPlazo = datos.obtenerInteger("diasPlazo");
		Integer codPagoSeguro = datos.obtenerInteger("codPagoSeguro");
		Integer codFondoContribEspecial = datos.obtenerInteger("codFondoContribEspecial");
		Integer codDestino = datos.obtenerInteger("codDestino");
		BigDecimal montoAsegurado = datos.obtenerBigDecimal("montoAsegurado");
		BigDecimal montoLimiteSubsidio = datos.obtenerBigDecimal("montoLimiteSubsidio");
		Integer senDisponibilidadSaldo = datos.obtenerInteger("senDisponibilidadSaldo");
		BigDecimal tasaPrimaTipoSeguro = datos.obtenerBigDecimal("tasaPrimaTipoSeguro");
		Integer codOficinaTran = datos.obtenerInteger("codOficinaTran");

		BigDecimal porcentajeCoberturaBFA = BigDecimal.ZERO;
		BigDecimal porcentajeCoberturaCliente = BigDecimal.ZERO;
		String codTabla = "";
		String nomDestino = "";
		Integer codGasto = 0;
		
		logger.debug("Validacion de campos requeridos para la operaci{on");
		UtileriaDeParametros.validarParametro(plazoMaximoDias,  NOM_COD_FNC + "plazoMaximoDias", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(diasPlazo,  NOM_COD_FNC + "diasPlazo", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codPagoSeguro,  NOM_COD_FNC + "codPagoSeguro", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codFondoContribEspecial,  NOM_COD_FNC + "codFondoContribEspecial", TipoValidacion.ENTERO_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(codDestino,  NOM_COD_FNC + "codDestino", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(montoAsegurado,  NOM_COD_FNC + "montoAsegurado", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(montoLimiteSubsidio,  NOM_COD_FNC + "montoLimiteSubsidio", TipoValidacion.BIGDECIMAL_MAYOR_IGUAL_CERO);
		UtileriaDeParametros.validarParametro(senDisponibilidadSaldo,  NOM_COD_FNC + "senDisponibilidadSaldo", TipoValidacion.ENTERO_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(tasaPrimaTipoSeguro,  NOM_COD_FNC + "tasaPrimaTipoSeguro", TipoValidacion.BIGDECIMAL_MAYOR_CERO);
		UtileriaDeParametros.validarParametro(codOficinaTran,  NOM_COD_FNC + "codOficinaTran", TipoValidacion.ENTERO_MAYOR_CERO);

		logger.debug("Evaluacion del perioso de dias de la poliza");
		
		Integer diasPeriodo = 0;
		Integer cantDiasAnio = Constantes.PP_ANIO_NO_BISIESTO;
		diasPeriodo = diasPlazo;
		
		if(UtileriaDeDatos.isGreater(diasPeriodo, plazoMaximoDias)) {
			diasPeriodo = plazoMaximoDias;
		}
		
		logger.debug("Verifica la forma de pago del seguro y recuperacion de valores SFBDB_PPMFS");
		
		if(!UtileriaDeDatos.isEquals(codPagoSeguro, new Integer(0))) {
			Object[] paramsPPMFS = {
					codFondoContribEspecial,
					codPagoSeguro
			};
			try {
				logger.debug(NOM_COD_FNC + "Ejecutando sentencia SELECT LINC SFBDB PPMFS, parametros: {}", Arrays.toString(paramsPPMFS));
				Map<String, Object> map_PMFS = jdbcTemplate.queryForMap(query(SELECT_SFBDB_PPMFS), paramsPPMFS);
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map_PMFS);
				codTabla = adaptador.getString("codTabla");
				codGasto = adaptador.getInteger("codGasto");
			} catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20019, "No existe {}", "RELACIÓN FORMA DE PAGO - SEGURO");
			}

			if(!UtileriaDeDatos.isBlank(codTabla)) {

				Object[] paramsBSMTG = {
						codTabla,
						codDestino
				};

				try {
					nomDestino = jdbcTemplate.queryForObject(query(SELECT_SFBDB_BSMTG), String.class, paramsBSMTG);
				} catch (EmptyResultDataAccessException e) {
					throw new ServicioException(20019, "No existe {}", "RELACIÓN FORMA DE PAGO - SEGURO");
				}
			}
		}
		
		logger.debug("Asignando parámetro de entrada a variables locales");
		
		BigDecimal limiteAsegurable = montoLimiteSubsidio;
		BigDecimal saldoClienteSubsidio = BigDecimal.ZERO;
		
		if(UtileriaDeDatos.isGreater(montoAsegurado, limiteAsegurable)) {
			montoAsegurado = limiteAsegurable;
		}
		
		if(UtileriaDeDatos.isEquals(senDisponibilidadSaldo, Constantes.SI)) {
			if(UtileriaDeDatos.isGreater(montoAsegurado, saldoClienteSubsidio)) {
				montoAsegurado = saldoClienteSubsidio;
			}
		}
		
		if(UtileriaDeDatos.isEquals(montoAsegurado, BigDecimal.ZERO)) {
			throw new ServicioException(20019, "No existe {}", "DISPONIBILIDAD DE SALDO PARA EL SUBSIDIO");
		}
		
		logger.debug("Cálculo del porcentaje de tasa y valor de la prima");
		
		BigDecimal porcentajeTasa = BigDecimal.ZERO;
		porcentajeTasa = tasaPrimaTipoSeguro;
		porcentajeTasa = porcentajeTasa.divide(new BigDecimal(100));
		
		BigDecimal factorTiempoAnios = BigDecimal.ZERO;
		BigDecimal valorPrima = BigDecimal.ZERO;
		
		if(UtileriaDeDatos.isGreater(diasPeriodo, cantDiasAnio)) {
			factorTiempoAnios = new BigDecimal(cantDiasAnio);
			factorTiempoAnios = (factorTiempoAnios.subtract(new BigDecimal(diasPeriodo))).abs();
			factorTiempoAnios = factorTiempoAnios.divide(new BigDecimal(cantDiasAnio), MathContext.DECIMAL128);
			factorTiempoAnios = factorTiempoAnios.multiply(Constantes.PP_FACTOR);
			factorTiempoAnios = factorTiempoAnios.add(new BigDecimal(1));
			porcentajeTasa = porcentajeTasa.multiply(factorTiempoAnios);
			valorPrima = montoAsegurado;
			valorPrima = valorPrima.multiply(porcentajeTasa);
		}else {
			porcentajeTasa = porcentajeTasa.divide(new BigDecimal(cantDiasAnio), MathContext.DECIMAL128);
			porcentajeTasa = porcentajeTasa.multiply(new BigDecimal(diasPeriodo));
			valorPrima = montoAsegurado;
			valorPrima = valorPrima.multiply(porcentajeTasa);
		}
		
		logger.debug("Calcular monto de subsidio cubierto por BFA");
		BigDecimal montoSubsidiaBFA = valorPrima;
		montoSubsidiaBFA = montoSubsidiaBFA.multiply(porcentajeCoberturaBFA.divide(new BigDecimal(100)));
		
		logger.debug("Evaluación de la forma de pago, verifica si hay subsidio del BFA");
		
		BigDecimal montoUtilizado = BigDecimal.ZERO;
		Integer cantidadCasos = 0;
		BigDecimal fondosDisponiblesRecuperado = BigDecimal.ZERO;
		if(!UtileriaDeDatos.isEquals(codPagoSeguro, Constantes.PP_PAGA_CLIENTE)) {
			
			Object[] paramsPPMCE = {
					codFondoContribEspecial,
					codOficinaTran,
					codDestino
			};
			
			try {
				Map<String, Object> map_PPMCE = jdbcTemplate.queryForMap(query(SELECT_SFBDB_PPMCE), paramsPPMCE );
				AdaptadorDeMapa adaptador = UtileriaDeDatos.adaptarMapa(map_PPMCE);
				
				montoUtilizado = adaptador.getBigDecimal("montoUtilizadoRecuperado");
				cantidadCasos = adaptador.getInteger("cantidadPrestamosBenef");
				fondosDisponiblesRecuperado = adaptador.getBigDecimal("fondosDisponiblesRecuperado");
				
				if(UtileriaDeDatos.isGreater(montoSubsidiaBFA, new BigDecimal(0))) {
					montoUtilizado = montoUtilizado.subtract(montoSubsidiaBFA);
					fondosDisponiblesRecuperado = fondosDisponiblesRecuperado.subtract(montoSubsidiaBFA);
				}
				
				if(UtileriaDeDatos.isGreater(montoSubsidiaBFA, fondosDisponiblesRecuperado)) {
					throw new ServicioException(20019, "No existe {}", "FONDOS INSUFICIENTES DEL BFA PARA SUBSIDIAR PRIMA");
				}
				
			} catch (EmptyResultDataAccessException e) {
				throw new ServicioException(20019, "No existe {}", "FONDO DE CONTRIBUCIÓN AGENCIA-DESTINO");
			}
		}
		
		datos.agregarDato("montoSubsidiaBFA", montoSubsidiaBFA.setScale(2, RoundingMode.HALF_UP));
		datos.agregarDato("valorPrima", valorPrima.setScale(2, RoundingMode.HALF_UP));
	}

}
