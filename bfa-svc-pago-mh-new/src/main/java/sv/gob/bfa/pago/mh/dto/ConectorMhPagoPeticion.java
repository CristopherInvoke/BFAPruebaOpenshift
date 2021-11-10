package sv.gob.bfa.pago.mh.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "conectoresServiciosMhPeticion")
public class ConectorMhPagoPeticion implements Serializable{


	private static final long serialVersionUID = 1L;
	
	private Long fechaPago;
	private String codBanco;
	private String codSucursal;
	private String codCaja;
	private Long transaccion;
	private String medioPago;
	private String tipoCodigo;
	private String codigo;
	
	public Long getFechaPago() {
		return fechaPago;
	}
	public void setFechaPago(Long fechaPago) {
		this.fechaPago = fechaPago;
	}
	public String getCodBanco() {
		return codBanco;
	}
	public void setCodBanco(String codBanco) {
		this.codBanco = codBanco;
	}
	public String getCodSucursal() {
		return codSucursal;
	}
	public void setCodSucursal(String codSucursal) {
		this.codSucursal = codSucursal;
	}
	public String getCodCaja() {
		return codCaja;
	}
	public void setCodCaja(String codCaja) {
		this.codCaja = codCaja;
	}
	public Long getTransaccion() {
		return transaccion;
	}
	public void setTransaccion(Long transaccion) {
		this.transaccion = transaccion;
	}
	public String getMedioPago() {
		return medioPago;
	}
	public void setMedioPago(String medioPago) {
		this.medioPago = medioPago;
	}
	public String getTipoCodigo() {
		return tipoCodigo;
	}
	public void setTipoCodigo(String tipoCodigo) {
		this.tipoCodigo = tipoCodigo;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

}

