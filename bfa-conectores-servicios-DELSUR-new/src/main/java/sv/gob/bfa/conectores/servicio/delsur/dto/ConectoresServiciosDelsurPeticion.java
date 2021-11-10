package sv.gob.bfa.conectores.servicio.delsur.dto;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name="ConectoresServiciosDelsurPeticion")
@ApiModel(description = "ConectoresServiciosDelsurPeticion")
public class ConectoresServiciosDelsurPeticion{

	private String metodo;
	private String npe;
	private String nis;
	private String referenciaDELSUR;
	private String codAgencia;
	private String codSucursal;
	private Integer pagoAlcaldia;
	private BigDecimal monto;
	
	public String getMetodo() {
		return metodo;
	}
	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}
	public String getNpe() {
		return npe;
	}
	public void setNpe(String npe) {
		this.npe = npe;
	}
	public String getNis() {
		return nis;
	}
	public void setNis(String nis) {
		this.nis = nis;
	}
	public String getReferenciaDELSUR() {
		return referenciaDELSUR;
	}
	public void setReferenciaDELSUR(String referenciaDELSUR) {
		this.referenciaDELSUR = referenciaDELSUR;
	}
	public String getCodAgencia() {
		return codAgencia;
	}
	public void setCodAgencia(String codAgencia) {
		this.codAgencia = codAgencia;
	}
	public String getCodSucursal() {
		return codSucursal;
	}
	public void setCodSucursal(String codSucursal) {
		this.codSucursal = codSucursal;
	}
	public Integer getPagoAlcaldia() {
		return pagoAlcaldia;
	}
	public void setPagoAlcaldia(Integer pagoAlcaldia) {
		this.pagoAlcaldia = pagoAlcaldia;
	}
	public BigDecimal getMonto() {
		return monto;
	}
	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}
	
}
