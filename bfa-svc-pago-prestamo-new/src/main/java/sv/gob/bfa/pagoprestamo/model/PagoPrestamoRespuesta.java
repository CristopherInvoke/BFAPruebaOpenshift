package sv.gob.bfa.pagoprestamo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

public class PagoPrestamoRespuesta extends Respuesta{

	private static final long serialVersionUID = 1L;
	
	private String nombreCompletoCliente;
	
	private String codCliente;
	
	private String duiCliente;
	
	private String lugarExpedicion;
	
	private String nombreDocumentoCliente;
	
	private Integer fechaExpedicion;
	
	private String nomCajero;
	
	private String nomOficinaTran;
	
	private String codPantalla;
	
	private Integer fechaSistema;
	
	private Integer fechaReal;
	
	private Integer fechaRelativa;
	
	private Integer horaSistema;
	
	private BigDecimal valorCapitalFacturadoRecibo;
	
	private BigDecimal valorInteresNormalFacturadoRecibo;
	
	private BigDecimal valorOtrosGastosFacturadosRecibo;
	
	private BigDecimal valorMoratorioFacturadoRecibo;
	
	private BigDecimal saldoTercerosDiaHoyRecibo;
	
	private BigDecimal valorTotalCobroRecibo;
	
	private BigDecimal valorTotalCapitalAbonadoRecibo;
	
	private BigDecimal valorTotalInteresCompensatorioAbonadoRecibo;
	
	private BigDecimal valorOtrosGastosAbonadosRecibo;
	
	private BigDecimal valorInteresAntesPagoRecibo;
	
	private BigDecimal valorInteresDespuesPagoRecibo;
	
	private BigDecimal valorTotalAbonoRecibo;
	
	private BigDecimal valorCapitalDiferenciaRecibo;
	
	private BigDecimal valorInteresDiferenciaRecibo;
	
	private BigDecimal valorInteresCompensatorioDiferenciaRecibo;
	
	private BigDecimal valorMoraDiferenciaRecibo;
	
	private BigDecimal valorOtrosGastosDiferenciaRecibo;
	
	private BigDecimal valorTotalDiferenciaRecibo;
	
	private BigDecimal valorDiferenciaTercerosRecibo;
	
	private BigDecimal valorTercerosAbonoRecibo;
	
	private BigDecimal saldoTercerosValorAnteriorRecibo;
	
	private String valorTotalCobroEnLetrasRecibo;
	
	private BigDecimal saldoRealRecibo;
	
	private BigDecimal saldoRealAnteriorRecibo;
	
	private String destinoPrestamo;
	
	private String nombreEstadoPrestamo;
	
	private BigDecimal valorInteresVencidoRecibo;
	
	private BigDecimal valorCompensatorioFacturado;
	
	private BigDecimal valorInteresMoraRecibo;
	
	private Integer numTran;
	
	private Integer codDestino;
	
	private String cuentaPrestamo;
	
	private String nomCompania;
	
	private Integer numDocumentoTran;
	
	private String codCajero;
	
	private Integer codTerminal;
	
	private String numeroCarpetaCliente;
	
	private String tecnicoCuenta;
	
	private Integer codCausalEfectivo;
	
	private Integer codCausalCheque;
	
	private Integer codCausalCtaCobrarBancaria;
	
	private Integer codCausalCompania;
	
	private Integer fechaVencimientoFactura;
	
	private BigDecimal tasaAnualInteresNormal;
	
	private BigDecimal valorMovimiento;
	
	private BigDecimal valorEfectivo;
	
	private BigDecimal valorCheques;
	
	private BigDecimal valorChequesPropios;
	
	private BigDecimal valorChequesAjenos;
	
	private String signoTercero1;
	
	private List<Cheque> cheques;
	
	private BigDecimal sumGastosCovid;

	private Integer OfiAdministrativa;
	
	public String getNombreCompletoCliente() {
		return nombreCompletoCliente;
	}

	public void setNombreCompletoCliente(String nombreCompletoCliente) {
		this.nombreCompletoCliente = nombreCompletoCliente;
	}

	public String getCodCliente() {
		return codCliente;
	}

	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}

	public String getDuiCliente() {
		return duiCliente;
	}

	public void setDuiCliente(String duiCliente) {
		this.duiCliente = duiCliente;
	}

	public String getLugarExpedicion() {
		return lugarExpedicion;
	}

	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public String getNombreDocumentoCliente() {
		return nombreDocumentoCliente;
	}

	public void setNombreDocumentoCliente(String nombreDocumentoCliente) {
		this.nombreDocumentoCliente = nombreDocumentoCliente;
	}

	public Integer getFechaExpedicion() {
		return fechaExpedicion;
	}

	public void setFechaExpedicion(Integer fechaExpedicion) {
		this.fechaExpedicion = fechaExpedicion;
	}

	public String getNomCajero() {
		return nomCajero;
	}

	public void setNomCajero(String nomCajero) {
		this.nomCajero = nomCajero;
	}

	public String getNomOficinaTran() {
		return nomOficinaTran;
	}

	public void setNomOficinaTran(String nomOficinaTran) {
		this.nomOficinaTran = nomOficinaTran;
	}

	public String getCodPantalla() {
		return codPantalla;
	}

	public void setCodPantalla(String codPantalla) {
		this.codPantalla = codPantalla;
	}

	public Integer getFechaSistema() {
		return fechaSistema;
	}

	public void setFechaSistema(Integer fechaSistema) {
		this.fechaSistema = fechaSistema;
	}

	public Integer getFechaReal() {
		return fechaReal;
	}

	public void setFechaReal(Integer fechaReal) {
		this.fechaReal = fechaReal;
	}

	public Integer getFechaRelativa() {
		return fechaRelativa;
	}

	public void setFechaRelativa(Integer fechaRelativa) {
		this.fechaRelativa = fechaRelativa;
	}

	public Integer getHoraSistema() {
		return horaSistema;
	}

	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}

	public BigDecimal getValorCapitalFacturadoRecibo() {
		return valorCapitalFacturadoRecibo;
	}

	public void setValorCapitalFacturadoRecibo(BigDecimal valorCapitalFacturadoRecibo) {
		this.valorCapitalFacturadoRecibo = valorCapitalFacturadoRecibo;
	}

	public BigDecimal getValorInteresNormalFacturadoRecibo() {
		return valorInteresNormalFacturadoRecibo;
	}

	public void setValorInteresNormalFacturadoRecibo(BigDecimal valorInteresNormalFacturadoRecibo) {
		this.valorInteresNormalFacturadoRecibo = valorInteresNormalFacturadoRecibo;
	}

	public BigDecimal getValorMoratorioFacturadoRecibo() {
		return valorMoratorioFacturadoRecibo;
	}

	public void setValorMoratorioFacturadoRecibo(BigDecimal valorMoratorioFacturadoRecibo) {
		this.valorMoratorioFacturadoRecibo = valorMoratorioFacturadoRecibo;
	}

	public BigDecimal getSaldoTercerosDiaHoyRecibo() {
		return saldoTercerosDiaHoyRecibo;
	}

	public void setSaldoTercerosDiaHoyRecibo(BigDecimal saldoTercerosDiaHoyRecibo) {
		this.saldoTercerosDiaHoyRecibo = saldoTercerosDiaHoyRecibo;
	}

	public BigDecimal getValorTotalCobroRecibo() {
		return valorTotalCobroRecibo;
	}

	public void setValorTotalCobroRecibo(BigDecimal valorTotalCobroRecibo) {
		this.valorTotalCobroRecibo = valorTotalCobroRecibo;
	}

	public BigDecimal getValorTotalCapitalAbonadoRecibo() {
		return valorTotalCapitalAbonadoRecibo;
	}

	public void setValorTotalCapitalAbonadoRecibo(BigDecimal valorTotalCapitalAbonadoRecibo) {
		this.valorTotalCapitalAbonadoRecibo = valorTotalCapitalAbonadoRecibo;
	}

	public BigDecimal getValorTotalInteresCompensatorioAbonadoRecibo() {
		return valorTotalInteresCompensatorioAbonadoRecibo;
	}

	public void setValorTotalInteresCompensatorioAbonadoRecibo(BigDecimal valorTotalInteresCompensatorioAbonadoRecibo) {
		this.valorTotalInteresCompensatorioAbonadoRecibo = valorTotalInteresCompensatorioAbonadoRecibo;
	}

	public BigDecimal getValorOtrosGastosAbonadosRecibo() {
		return valorOtrosGastosAbonadosRecibo;
	}

	public void setValorOtrosGastosAbonadosRecibo(BigDecimal valorOtrosGastosAbonadosRecibo) {
		this.valorOtrosGastosAbonadosRecibo = valorOtrosGastosAbonadosRecibo;
	}

	public BigDecimal getValorInteresAntesPagoRecibo() {
		return valorInteresAntesPagoRecibo;
	}

	public void setValorInteresAntesPagoRecibo(BigDecimal valorInteresAntesPagoRecibo) {
		this.valorInteresAntesPagoRecibo = valorInteresAntesPagoRecibo;
	}

	public BigDecimal getValorInteresDespuesPagoRecibo() {
		return valorInteresDespuesPagoRecibo;
	}

	public void setValorInteresDespuesPagoRecibo(BigDecimal valorInteresDespuesPagoRecibo) {
		this.valorInteresDespuesPagoRecibo = valorInteresDespuesPagoRecibo;
	}

	public BigDecimal getValorTotalAbonoRecibo() {
		return valorTotalAbonoRecibo;
	}

	public void setValorTotalAbonoRecibo(BigDecimal valorTotalAbonoRecibo) {
		this.valorTotalAbonoRecibo = valorTotalAbonoRecibo;
	}

	public BigDecimal getValorCapitalDiferenciaRecibo() {
		return valorCapitalDiferenciaRecibo;
	}

	public void setValorCapitalDiferenciaRecibo(BigDecimal valorCapitalDiferenciaRecibo) {
		this.valorCapitalDiferenciaRecibo = valorCapitalDiferenciaRecibo;
	}

	public BigDecimal getValorInteresDiferenciaRecibo() {
		return valorInteresDiferenciaRecibo;
	}

	public void setValorInteresDiferenciaRecibo(BigDecimal valorInteresDiferenciaRecibo) {
		this.valorInteresDiferenciaRecibo = valorInteresDiferenciaRecibo;
	}

	public BigDecimal getValorInteresCompensatorioDiferenciaRecibo() {
		return valorInteresCompensatorioDiferenciaRecibo;
	}

	public void setValorInteresCompensatorioDiferenciaRecibo(BigDecimal valorInteresCompensatorioDiferenciaRecibo) {
		this.valorInteresCompensatorioDiferenciaRecibo = valorInteresCompensatorioDiferenciaRecibo;
	}

	public BigDecimal getValorMoraDiferenciaRecibo() {
		return valorMoraDiferenciaRecibo;
	}

	public void setValorMoraDiferenciaRecibo(BigDecimal valorMoraDiferenciaRecibo) {
		this.valorMoraDiferenciaRecibo = valorMoraDiferenciaRecibo;
	}

	public BigDecimal getValorOtrosGastosDiferenciaRecibo() {
		return valorOtrosGastosDiferenciaRecibo;
	}

	public void setValorOtrosGastosDiferenciaRecibo(BigDecimal valorOtrosGastosDiferenciaRecibo) {
		this.valorOtrosGastosDiferenciaRecibo = valorOtrosGastosDiferenciaRecibo;
	}

	public BigDecimal getValorTotalDiferenciaRecibo() {
		return valorTotalDiferenciaRecibo;
	}

	public void setValorTotalDiferenciaRecibo(BigDecimal valorTotalDiferenciaRecibo) {
		this.valorTotalDiferenciaRecibo = valorTotalDiferenciaRecibo;
	}

	public BigDecimal getValorDiferenciaTercerosRecibo() {
		return valorDiferenciaTercerosRecibo;
	}

	public void setValorDiferenciaTercerosRecibo(BigDecimal valorDiferenciaTercerosRecibo) {
		this.valorDiferenciaTercerosRecibo = valorDiferenciaTercerosRecibo;
	}

	public BigDecimal getValorTercerosAbonoRecibo() {
		return valorTercerosAbonoRecibo;
	}

	public void setValorTercerosAbonoRecibo(BigDecimal valorTercerosAbonoRecibo) {
		this.valorTercerosAbonoRecibo = valorTercerosAbonoRecibo;
	}

	public BigDecimal getSaldoTercerosValorAnteriorRecibo() {
		return saldoTercerosValorAnteriorRecibo;
	}

	public void setSaldoTercerosValorAnteriorRecibo(BigDecimal saldoTercerosValorAnteriorRecibo) {
		this.saldoTercerosValorAnteriorRecibo = saldoTercerosValorAnteriorRecibo;
	}

	public String getValorTotalCobroEnLetrasRecibo() {
		return valorTotalCobroEnLetrasRecibo;
	}

	public void setValorTotalCobroEnLetrasRecibo(String valorTotalCobroEnLetrasRecibo) {
		this.valorTotalCobroEnLetrasRecibo = valorTotalCobroEnLetrasRecibo;
	}

	public BigDecimal getSaldoRealRecibo() {
		return saldoRealRecibo;
	}

	public void setSaldoRealRecibo(BigDecimal saldoRealRecibo) {
		this.saldoRealRecibo = saldoRealRecibo;
	}

	public String getDestinoPrestamo() {
		return destinoPrestamo;
	}

	public void setDestinoPrestamo(String destinoPrestamo) {
		this.destinoPrestamo = destinoPrestamo;
	}

	public String getNombreEstadoPrestamo() {
		return nombreEstadoPrestamo;
	}

	public void setNombreEstadoPrestamo(String nombreEstadoPrestamo) {
		this.nombreEstadoPrestamo = nombreEstadoPrestamo;
	}

	public BigDecimal getValorInteresVencidoRecibo() {
		return valorInteresVencidoRecibo;
	}

	public void setValorInteresVencidoRecibo(BigDecimal valorInteresVencidoRecibo) {
		this.valorInteresVencidoRecibo = valorInteresVencidoRecibo;
	}

	public BigDecimal getValorCompensatorioFacturado() {
		return valorCompensatorioFacturado;
	}

	public void setValorCompensatorioFacturado(BigDecimal valorCompensatorioFacturado) {
		this.valorCompensatorioFacturado = valorCompensatorioFacturado;
	}

	public BigDecimal getValorInteresMoraRecibo() {
		return valorInteresMoraRecibo;
	}

	public void setValorInteresMoraRecibo(BigDecimal valorInteresMoraRecibo) {
		this.valorInteresMoraRecibo = valorInteresMoraRecibo;
	}
	
	public Integer getNumTran() {
		return numTran;
	}

	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}
	
	public String getCuentaPrestamo() {
		return cuentaPrestamo;
	}

	public void setCuentaPrestamo(String cuentaPrestamo) {
		this.cuentaPrestamo = cuentaPrestamo;
	}
	
	public BigDecimal getValorOtrosGastosFacturadosRecibo() {
		return valorOtrosGastosFacturadosRecibo;
	}

	public Integer getCodDestino() {
		return codDestino;
	}

	public String getNomCompania() {
		return nomCompania;
	}

	public void setValorOtrosGastosFacturadosRecibo(BigDecimal valorOtrosGastosFacturadosRecibo) {
		this.valorOtrosGastosFacturadosRecibo = valorOtrosGastosFacturadosRecibo;
	}

	public void setCodDestino(Integer codDestino) {
		this.codDestino = codDestino;
	}

	public void setNomCompania(String nomCompania) {
		this.nomCompania = nomCompania;
	}

	public BigDecimal getSaldoRealAnteriorRecibo() {
		return saldoRealAnteriorRecibo;
	}

	public void setSaldoRealAnteriorRecibo(BigDecimal saldoRealAnteriorRecibo) {
		this.saldoRealAnteriorRecibo = saldoRealAnteriorRecibo;
	}

	public void setCheques(ArrayList<Cheque> cheques) {
		this.cheques = cheques;
	}
	
	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}

	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}
	
	public Integer getCodTerminal() {
		return codTerminal;
	}

	public void setCodTerminal(Integer codTerminal) {
		this.codTerminal = codTerminal;
	}

	public String getCodCajero() {
		return codCajero;
	}

	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}
	
	public List<Cheque> getCheques() {
		return cheques;
	}

	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}

	public String getNumeroCarpetaCliente() {
		return numeroCarpetaCliente;
	}

	public void setNumeroCarpetaCliente(String numeroCarpetaCliente) {
		this.numeroCarpetaCliente = numeroCarpetaCliente;
	}

	public String getTecnicoCuenta() {
		return tecnicoCuenta;
	}

	public void setTecnicoCuenta(String tecnicoCuenta) {
		this.tecnicoCuenta = tecnicoCuenta;
	}

	public Integer getCodCausalEfectivo() {
		return codCausalEfectivo;
	}

	public void setCodCausalEfectivo(Integer codCausalEfectivo) {
		this.codCausalEfectivo = codCausalEfectivo;
	}

	public Integer getCodCausalCheque() {
		return codCausalCheque;
	}

	public void setCodCausalCheque(Integer codCausalCheque) {
		this.codCausalCheque = codCausalCheque;
	}

	public Integer getFechaVencimientoFactura() {
		return fechaVencimientoFactura;
	}

	public void setFechaVencimientoFactura(Integer fechaVencimientoFactura) {
		this.fechaVencimientoFactura = fechaVencimientoFactura;
	}

	public BigDecimal getTasaAnualInteresNormal() {
		return tasaAnualInteresNormal;
	}

	public void setTasaAnualInteresNormal(BigDecimal tasaAnualInteresNormal) {
		this.tasaAnualInteresNormal = tasaAnualInteresNormal;
	}
	
	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}

	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}

	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}

	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}

	public BigDecimal getValorCheques() {
		return valorCheques;
	}

	public void setValorCheques(BigDecimal valorCheques) {
		this.valorCheques = valorCheques;
	}

	public BigDecimal getValorChequesPropios() {
		return valorChequesPropios;
	}

	public void setValorChequesPropios(BigDecimal valorChequesPropios) {
		this.valorChequesPropios = valorChequesPropios;
	}

	public BigDecimal getValorChequesAjenos() {
		return valorChequesAjenos;
	}

	public void setValorChequesAjenos(BigDecimal valorChequesAjenos) {
		this.valorChequesAjenos = valorChequesAjenos;
	}

	public Integer getCodCausalCtaCobrarBancaria() {
		return codCausalCtaCobrarBancaria;
	}

	public void setCodCausalCtaCobrarBancaria(Integer codCausalCtaCobrarBancaria) {
		this.codCausalCtaCobrarBancaria = codCausalCtaCobrarBancaria;
	}

	public Integer getCodCausalCompania() {
		return codCausalCompania;
	}

	public void setCodCausalCompania(Integer codCausalCompania) {
		this.codCausalCompania = codCausalCompania;
	}

	public String getSignoTercero1() {
		return signoTercero1;
	}

	public void setSignoTercero1(String signoTercero1) {
		this.signoTercero1 = signoTercero1;
	}

	@Override
	public String toString() {
		return "PagoPrestamoRespuesta [nombreCompletoCliente=" + nombreCompletoCliente + ", codCliente=" + codCliente
				+ ", duiCliente=" + duiCliente + ", lugarExpedicion=" + lugarExpedicion + ", nombreDocumentoCliente="
				+ nombreDocumentoCliente + ", fechaExpedicion=" + fechaExpedicion + ", nomCajero=" + nomCajero
				+ ", nomOficinaTran=" + nomOficinaTran + ", codPantalla=" + codPantalla + ", fechaSistema="
				+ fechaSistema + ", fechaReal=" + fechaReal + ", fechaRelativa=" + fechaRelativa + ", horaSistema="
				+ horaSistema + ", valorCapitalFacturadoRecibo=" + valorCapitalFacturadoRecibo
				+ ", valorInteresNormalFacturadoRecibo=" + valorInteresNormalFacturadoRecibo
				+ ", valorOtrosGastosFacturadosRecibo=" + valorOtrosGastosFacturadosRecibo
				+ ", valorMoratorioFacturadoRecibo=" + valorMoratorioFacturadoRecibo + ", saldoTercerosDiaHoyRecibo="
				+ saldoTercerosDiaHoyRecibo + ", valorTotalCobroRecibo=" + valorTotalCobroRecibo
				+ ", valorTotalCapitalAbonadoRecibo=" + valorTotalCapitalAbonadoRecibo
				+ ", valorTotalInteresCompensatorioAbonadoRecibo=" + valorTotalInteresCompensatorioAbonadoRecibo
				+ ", valorOtrosGastosAbonadosRecibo=" + valorOtrosGastosAbonadosRecibo
				+ ", valorInteresAntesPagoRecibo=" + valorInteresAntesPagoRecibo + ", valorInteresDespuesPagoRecibo="
				+ valorInteresDespuesPagoRecibo + ", valorTotalAbonoRecibo=" + valorTotalAbonoRecibo
				+ ", valorCapitalDiferenciaRecibo=" + valorCapitalDiferenciaRecibo + ", valorInteresDiferenciaRecibo="
				+ valorInteresDiferenciaRecibo + ", valorInteresCompensatorioDiferenciaRecibo="
				+ valorInteresCompensatorioDiferenciaRecibo + ", valorMoraDiferenciaRecibo=" + valorMoraDiferenciaRecibo
				+ ", valorOtrosGastosDiferenciaRecibo=" + valorOtrosGastosDiferenciaRecibo
				+ ", valorTotalDiferenciaRecibo=" + valorTotalDiferenciaRecibo + ", valorDiferenciaTercerosRecibo="
				+ valorDiferenciaTercerosRecibo + ", valorTercerosAbonoRecibo=" + valorTercerosAbonoRecibo
				+ ", saldoTercerosValorAnteriorRecibo=" + saldoTercerosValorAnteriorRecibo
				+ ", valorTotalCobroEnLetrasRecibo=" + valorTotalCobroEnLetrasRecibo + ", saldoRealRecibo="
				+ saldoRealRecibo + ", saldoRealAnteriorRecibo=" + saldoRealAnteriorRecibo + ", destinoPrestamo="
				+ destinoPrestamo + ", nombreEstadoPrestamo=" + nombreEstadoPrestamo + ", valorInteresVencidoRecibo="
				+ valorInteresVencidoRecibo + ", valorCompensatorioFacturado=" + valorCompensatorioFacturado
				+ ", valorInteresMoraRecibo=" + valorInteresMoraRecibo + ", numTran=" + numTran + ", codDestino="
				+ codDestino + ", cuentaPrestamo=" + cuentaPrestamo + ", nomCompania=" + nomCompania
				+ ", numDocumentoTran=" + numDocumentoTran + ", codCajero=" + codCajero + ", codTerminal=" + codTerminal
				+ ", numeroCarpetaCliente=" + numeroCarpetaCliente + ", tecnicoCuenta=" + tecnicoCuenta
				+ ", codCausalEfectivo=" + codCausalEfectivo + ", codCausalCheque=" + codCausalCheque
				+ ", codCausalCtaCobrarBancaria=" + codCausalCtaCobrarBancaria + ", codCausalCompania="
				+ codCausalCompania + ", fechaVencimientoFactura=" + fechaVencimientoFactura
				+ ", tasaAnualInteresNormal=" + tasaAnualInteresNormal + ", valorMovimiento=" + valorMovimiento
				+ ", valorEfectivo=" + valorEfectivo + ", valorCheques=" + valorCheques + ", valorChequesPropios="
				+ valorChequesPropios + ", valorChequesAjenos=" + valorChequesAjenos + ", cheques=" + cheques + "]";
	}

	public BigDecimal getSumGastosCovid() {
		return sumGastosCovid;
	}

	public void setSumGastosCovid(BigDecimal sumGastosCovid) {
		this.sumGastosCovid = sumGastosCovid;
	}

	public Integer getOfiAdministrativa() {
		return OfiAdministrativa;
	}

	public void setOfiAdministrativa(Integer ofiAdministrativa) {
		OfiAdministrativa = ofiAdministrativa;
	}

}
