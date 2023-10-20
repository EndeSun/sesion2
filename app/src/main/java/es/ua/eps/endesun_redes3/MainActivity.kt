@file:Suppress("DEPRECATION")

package es.ua.eps.endesun_redes3

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST_CODE = 101
        private val REQUEST_PERMISSION = 1
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textInfo = findViewById<TextView>(R.id.infoSIM)

        // Hay permiso, accedemos a la información de la SIM y de la red
        if (checkPermission()) {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val builder = StringBuilder()
            builder.append("Nombre del operador: " + getOperatorName(telephonyManager))
            builder.append("\n\n")
            builder.append("Line number: " + getLineNumber(telephonyManager))
            builder.append("\n\n")
            builder.append("Coutry code: " + getCountryCode(telephonyManager))
            builder.append("\n\n")
            val ICCID = try {
                getSimSerialNumber(telephonyManager)
            }catch(e: SecurityException) {
                "No se pudo obtener la información de ICCID"
            }
            builder.append("ICCID: " + ICCID)
            builder.append("\n\n")
            val IMSI = try {
                getSubscriberId(telephonyManager)
            }catch(e: SecurityException) {
                "No se pudo obtener la información del IMSI"
            }
            builder.append("IMSI: " + IMSI)
            builder.append("\n\n")
            builder.append("Tipo de red: " + getNetworkType(telephonyManager))
            builder.append("\n\n")
            val IMEI = try {
                getDeviceId(telephonyManager)
            }catch(e: SecurityException) {
                "No se pudo obtener la información del IMEI"
            }
            builder.append("IMEI: " + IMEI)
            builder.append("\n\n")
            builder.append("Phone type: " + getPhoneType(telephonyManager))
            builder.append("\n\n")
            builder.append("Cell Information: "+ getCellInfo(this, telephonyManager))

            textInfo.text  = builder.toString()
        }
    }
    private fun checkPermission(): Boolean {
        val readPhoneStatePermission = Manifest.permission.READ_PHONE_STATE
        val readSmsPermission = Manifest.permission.READ_SMS
        val readPhoneNumbersPermission = Manifest.permission.READ_PHONE_NUMBERS
        //Si alguno de los permisos no están aceptados, se cancela devolviendo false
        if (ContextCompat.checkSelfPermission(this, readPhoneStatePermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, readSmsPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, readPhoneNumbersPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(readPhoneStatePermission, readSmsPermission, readPhoneNumbersPermission), PERMISSION_REQUEST_CODE)
            return false
        }
        //todo ok, permisos obtenidos
        return true
    }
    // Public methods

    //devuelve el ICCID
    fun getSimSerialNumber(telephonyManager: TelephonyManager): String {
        return telephonyManager.simSerialNumber ?: "N/A"
    }
    //devuelve la IMSI
    fun getSubscriberId(telephonyManager: TelephonyManager): String {
        return telephonyManager.subscriberId ?: "N/A"
    }
    //    devuelve el IMEI
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDeviceId(telephonyManager: TelephonyManager): String {
        return telephonyManager.imei ?: "N/A"
    }

//    devuelve el nombre del operador
    fun getOperatorName(telephonyManager: TelephonyManager): String {
        return telephonyManager.simOperatorName ?: "N/A"
    }
//    devuelve el tipo de red
    fun getNetworkType(telephonyManager: TelephonyManager): String {
        return telephonyManager.networkOperatorName ?: "N/A"
    }

    fun getPhoneType(telephonyManager: TelephonyManager): String {
        val phoneType = when (telephonyManager.phoneType) {
            TelephonyManager.PHONE_TYPE_GSM -> "GSM"
            TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
            else -> "Unknown"
        }
        return phoneType
    }
    @SuppressLint("MissingPermission")
    fun getLineNumber(telephonyManager: TelephonyManager): String {
        return telephonyManager.line1Number ?: "N/A"
    }
    fun getCountryCode(telephonyManager: TelephonyManager): String {
        return telephonyManager.simCountryIso ?: "N/A"
    }
    fun getCellInfo(context: Context, telephonyManager: TelephonyManager): String {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val cellInfoList: List<CellInfo> = telephonyManager.allCellInfo
            for (cellInfo in cellInfoList) {
                if (cellInfo is CellInfoGsm) {
                    val cellIdentity = cellInfo.cellIdentity
                    val cellId = cellIdentity.cid
                    val lac = cellIdentity.lac
                    return "Cell ID: $cellId, LAC: $lac"
                } else if (cellInfo is CellInfoLte) {
                    val cellIdentity = cellInfo.cellIdentity
                    val cellId = cellIdentity.ci
                    val tac = cellIdentity.tac
                    return "Cell ID: $cellId, LAC: $tac"
                } else if (cellInfo is CellInfoWcdma) {
                    val cellIdentity = cellInfo.cellIdentity
                    val cellId = cellIdentity.cid
                    val lac = cellIdentity.lac
                    return "Cell ID: $cellId, LAC: $lac"
                }
            }
            return "No se pudo obtener información de la celda."
        } else {
            return "Permiso de ubicación no otorgado."
        }
    }
}