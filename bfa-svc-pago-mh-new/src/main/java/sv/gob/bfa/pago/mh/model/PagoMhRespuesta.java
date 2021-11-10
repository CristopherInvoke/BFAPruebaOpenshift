package sv.gob.bfa.pago.mh.model;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Respuesta;

/**
 * Clase correspondiente a la respuesta que el servicio
 * Pago Ministerio de Hacienda En Linea debe retornar.
 */
@XmlRootElement(name="pagoMhRespuesta")
public class PagoMhRespuesta extends Respuesta{
	
	private static final long serialVersionUID = 1L;
	

	private Integer fechaSistema;
	private Integer horaSistema;
	private Integer idTransaccionMh;
	private Integer numTran;
	private Integer numDocumentoTran;
	private String nomCajero;
	private String nomOficinaTran;
	private String codBarra;
	private String npe;
	
	public Integer getFechaSistema() {
		return fechaSistema;
	}

	public void setFechaSistema(Integer fechaSistema) {
		this.fechaSistema = fechaSistema;
	}

	public Integer getHoraSistema() {
		return horaSistema;
	}

	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}

	public Integer getIdTransaccionMh() {
		return idTransaccionMh;
	}
	
	public void setIdTransaccionMh(Integer idTransaccionMh) {
		this.idTransaccionMh = idTransaccionMh;
	}
	
	public Integer getNumTran() {
		return numTran;
	}
	
	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}
	
	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}
	
	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
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

	public String getCodBarra() {
		return codBarra;
	}

	public void setCodBarra(String codBarra) {
		this.codBarra = codBarra;
	}

	public String getNpe() {
		return npe;
	}

	public void setNpe(String npe) {
		this.npe = npe;
	}
	
}
