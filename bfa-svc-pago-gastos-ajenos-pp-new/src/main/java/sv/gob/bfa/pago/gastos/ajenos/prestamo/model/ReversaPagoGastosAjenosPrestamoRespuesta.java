package sv.gob.bfa.pago.gastos.ajenos.prestamo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

/**
 * Clase correspondiente a la respuesta que el servicio
 * Reversa Deposito Mixto Cuenta Corriente debe retornar.
 */
@XmlRootElement(name="ReversaDepositoMixtoCuentaCorrienteRespuesta")
public class ReversaPagoGastosAjenosPrestamoRespuesta extends Respuesta{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Nombre del cliente
	 */
	private String nomCliente;
	
	/**
	 * c&oacutedigo del cliente
	 */
	private String codCliente;
	
	/**
	 * N&uactemero de DUI de cliente
	 */
	
	/**
	 * Nombre de cajero
	 */
	private String nomCajero;
	
	/**
	 * Nombre de la oficina donde se realiza la transacci&oacuten
	 */
	private String nomOficina;
	
	/**
	 * C&oacutedigo de pantalla 
	 */
	private String codPantalla;
	
	/**
	 * Fecha de Sistema
	 */
	private Integer fechaSistema;
	
	/**
	 * Fecha Real
	 */
	private Integer fechaReal;
	
	/**
	 * Fecha Relativa
	 */
	private Integer fechaRelativa;
	
	/**
	 * Hora del sistema 
	 */
	private Integer horaSistema;

	/**
	 * Arreglo de objetos de tipo cheque
	*/
	private List<Cheque> cheques;
	
	public String getNomCliente() {
		return nomCliente;
	}
	public void setNomCliente(String nomCliente) {
		this.nomCliente = nomCliente;
	}
	public String getCodCliente() {
		return codCliente;
	}
	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}
	public String getNomCajero() {
		return nomCajero;
	}
	public void setNomCajero(String nomCajero) {
		this.nomCajero = nomCajero;
	}
	public String getNomOficina() {
		return nomOficina;
	}
	public void setNomOficina(String nomOficina) {
		this.nomOficina = nomOficina;
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
	public List<Cheque> getCheques() {
		return cheques;
	}
	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	
}
