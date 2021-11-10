
package sv.com.delsur;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pIdTransaccion" type="{http://www.w3.org/2001/XMLSchema}long" form="qualified"/&gt;
 *         &lt;element name="pNisRad" type="{http://www.w3.org/2001/XMLSchema}long" form="qualified"/&gt;
 *         &lt;element name="pCodAgencia" type="{http://www.w3.org/2001/XMLSchema}long" form="qualified"/&gt;
 *         &lt;element name="pCodSucursal" type="{http://www.w3.org/2001/XMLSchema}long" form="qualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pIdTransaccion",
    "pNisRad",
    "pCodAgencia",
    "pCodSucursal"
})
@XmlRootElement(name = "Anular_Pago")
public class AnularPago {

    protected long pIdTransaccion;
    protected long pNisRad;
    protected long pCodAgencia;
    protected long pCodSucursal;

    /**
     * Gets the value of the pIdTransaccion property.
     * 
     */
    public long getPIdTransaccion() {
        return pIdTransaccion;
    }

    /**
     * Sets the value of the pIdTransaccion property.
     * 
     */
    public void setPIdTransaccion(long value) {
        this.pIdTransaccion = value;
    }

    /**
     * Gets the value of the pNisRad property.
     * 
     */
    public long getPNisRad() {
        return pNisRad;
    }

    /**
     * Sets the value of the pNisRad property.
     * 
     */
    public void setPNisRad(long value) {
        this.pNisRad = value;
    }

    /**
     * Gets the value of the pCodAgencia property.
     * 
     */
    public long getPCodAgencia() {
        return pCodAgencia;
    }

    /**
     * Sets the value of the pCodAgencia property.
     * 
     */
    public void setPCodAgencia(long value) {
        this.pCodAgencia = value;
    }

    /**
     * Gets the value of the pCodSucursal property.
     * 
     */
    public long getPCodSucursal() {
        return pCodSucursal;
    }

    /**
     * Sets the value of the pCodSucursal property.
     * 
     */
    public void setPCodSucursal(long value) {
        this.pCodSucursal = value;
    }

}
