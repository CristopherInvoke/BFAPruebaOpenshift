package sv.gob.bfa.pagocomision.model;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

/**
 * Clase correspondiente a la respuesta que el servicio
 * Pagos de Cheques de Gerencia debe retornar.
 */
@XmlRootElement(name="PagoComisionRespuesta")
public class PagoComisionRespuesta extends Respuesta{

	private Integer      codigo;	
	private String 		 descripcion;	
	private Integer 	 numDocumentoTran;
	private String 		 cuentaTransaccion;
	private String 		 codCajero;
	private String 		 nomCajero;
	private Integer 	 codOficinaTran;
	private String 		 nomOficinaTran;
	private String 		 codPantalla;
	private Integer 	 fechaSistema;
	private Integer 	 fechaReal;
	private Integer 	 horaSistema;
	private Integer 	 fechaRelativa;
	private BigDecimal 	 valorEfectivo;
	private BigDecimal 	 valorCheques;
	private BigDecimal   valorChequesPropios;
	private BigDecimal   valorChequesAjenos;
	private BigDecimal   valorChequesExt;
	private BigDecimal 	 valorMovimiento;
	private Integer 	 numTran;
	private Integer      senCreditoFiscal;
	private String 		 codCliente;
	private List<Cheque> cheques;

	
	public Integer getCodigo() {
		return codigo;
	}


	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}


	public String getDescripcion() {
		return descripcion;
	}


	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}


	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}


	public String getCuentaTransaccion() {
		return cuentaTransaccion;
	}


	public void setCuentaTransaccion(String cuentaTransaccion) {
		this.cuentaTransaccion = cuentaTransaccion;
	}


	public String getCodCajero() {
		return codCajero;
	}


	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}


	public String getNomCajero() {
		return nomCajero;
	}


	public void setNomCajero(String nomCajero) {
		this.nomCajero = nomCajero;
	}


	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}


	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
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


	public Integer getHoraSistema() {
		return horaSistema;
	}


	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}


	public Integer getFechaRelativa() {
		return fechaRelativa;
	}


	public void setFechaRelativa(Integer fechaRelativa) {
		this.fechaRelativa = fechaRelativa;
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


	public BigDecimal getValorChequesExt() {
		return valorChequesExt;
	}


	public void setValorChequesExt(BigDecimal valorChequesExt) {
		this.valorChequesExt = valorChequesExt;
	}



	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}


	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}


	public Integer getNumTran() {
		return numTran;
	}


	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}	

	public Integer getSenCreditoFiscal() {
		return senCreditoFiscal;
	}

	public void setSenCreditoFiscal(Integer senCreditoFiscal) {
		this.senCreditoFiscal = senCreditoFiscal;
	}
	
	
	public String getCodCliente() {
		return codCliente;
	}


	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}


	public List<Cheque> getCheques() {
		return cheques;
	}


	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}


	@Override
	public String toString() {
		return "IngresoCertificadoRespuesta "
				+ "["
				+ "	 codigo=" 					+ codigo 
				+ ", descripcion=" 				+ descripcion
				+ ", numDocumentoTran"			+ numDocumentoTran
				+ ", cuentaTransaccion="		+ cuentaTransaccion
				+ ", codCajero=" 				+ codCajero 
				+ ", nomCajero="				+ nomCajero 
				+ ", codOficinaTran="			+ codOficinaTran
				+ ", nomOficinaTran=" 			+ nomOficinaTran
				+ ", codPantalla=" 				+ codPantalla 
				+ ", fechaSistema=" 			+ fechaSistema 
				+ ", fechaReal=" 				+ fechaReal
				+ ", horaSistema="				+ horaSistema 
				+ ", fechaRelativa"				+ fechaRelativa
				+ ", valorEfectivo=" 			+ valorEfectivo 
				+ ", valorCheques=" 			+ valorCheques
				+ ", valorMovimiento=" 			+ valorMovimiento 
				+ ", numTran=" 					+ numTran
				+ ", senCreditoFiscal="			+ senCreditoFiscal
				+ ", codCliente="				+ codCliente
				+ ", cheques="					+ cheques
				+ "]";
	}

}
