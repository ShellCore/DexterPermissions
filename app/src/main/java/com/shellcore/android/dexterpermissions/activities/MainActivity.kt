package com.shellcore.android.dexterpermissions.activities

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import com.shellcore.android.dexterpermissions.R
import com.shellcore.android.dexterpermissions.enums.PermissionStatusEnum
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBtnClicks()
    }

    private fun setBtnClicks() {
        btnCamera.setOnClickListener { checkCameraPermissions() }
        btnContacts.setOnClickListener { checkContactsPermissions() }
        btnAudio.setOnClickListener { checkAudioPermissions() }
        btnAll.setOnClickListener { checkAllPermissions() }
    }

//    private fun checkCameraPermissions() = setPermissionHandler(Manifest.permission.CAMERA, txtCamera)
//    private fun checkCameraPermissions() = setCameraPermissionHandlerWithDialog()
    private fun checkCameraPermissions() = setCameraPermissionHandlerWithSnackbar()

    private fun checkContactsPermissions() = setPermissionHandler(Manifest.permission.READ_CONTACTS, txtContacts)

    private fun checkAudioPermissions() = setPermissionHandler(Manifest.permission.RECORD_AUDIO, txtAudio)

    fun checkAllPermissions() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            for (permission in report.grantedPermissionResponses) {
                                when (permission.permissionName) {
                                    Manifest.permission.CAMERA -> setPermissionStatus(txtCamera, PermissionStatusEnum.GRANTED)
                                    Manifest.permission.READ_CONTACTS -> setPermissionStatus(txtContacts, PermissionStatusEnum.GRANTED)
                                    Manifest.permission.RECORD_AUDIO -> setPermissionStatus(txtAudio, PermissionStatusEnum.GRANTED)
                                }
                            }
                            for (permission in report.deniedPermissionResponses) {
                                when (permission.permissionName) {
                                    Manifest.permission.CAMERA -> {
                                        if (permission.isPermanentlyDenied) {
                                            setPermissionStatus(txtCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                                        } else {
                                            setPermissionStatus(txtCamera, PermissionStatusEnum.DENIED)
                                        }
                                    }
                                    Manifest.permission.READ_CONTACTS -> {
                                        if (permission.isPermanentlyDenied) {
                                            setPermissionStatus(txtContacts, PermissionStatusEnum.PERMANENTLY_DENIED)
                                        } else {
                                            setPermissionStatus(txtContacts, PermissionStatusEnum.DENIED)
                                        }
                                    }
                                    Manifest.permission.RECORD_AUDIO -> {
                                        if (permission.isPermanentlyDenied) {
                                            setPermissionStatus(txtAudio, PermissionStatusEnum.PERMANENTLY_DENIED)
                                        } else {
                                            setPermissionStatus(txtAudio, PermissionStatusEnum.DENIED)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }

                })
                .check()
    }

    private fun setPermissionHandler(permission: String, txtView: TextView) {
        Dexter.withActivity(this)
                .withPermission(permission)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        setPermissionStatus(txtView, PermissionStatusEnum.GRANTED)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            setPermissionStatus(txtView, PermissionStatusEnum.PERMANENTLY_DENIED)
                        } else {
                            setPermissionStatus(txtView, PermissionStatusEnum.DENIED)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                })
                .check()
    }

    private fun setPermissionStatus(txtView: TextView, status: PermissionStatusEnum) {
        when (status) {
            PermissionStatusEnum.GRANTED -> {
                txtView.text = getString(R.string.permission_granted)
                txtView.setTextColor(ContextCompat.getColor(this, R.color.granted))
            }
            PermissionStatusEnum.PERMANENTLY_DENIED-> {
                txtView.text = getString(R.string.permission_permanently_denied)
                txtView.setTextColor(ContextCompat.getColor(this, R.color.denied))
            }
            PermissionStatusEnum.DENIED -> {
                txtView.text = getString(R.string.permission_denied)
                txtView.setTextColor(ContextCompat.getColor(this, R.color.denied))
            }

        }
    }

    private fun setCameraPermissionHandlerWithDialog() {
        val dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                .withContext(this)
                .withTitle("Camera Permission")
                .withMessage("Camera permission is needed to take pictures")
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build()

        val permission = object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setPermissionStatus(txtCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                if (response.isPermanentlyDenied) {
                    setPermissionStatus(txtCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus(txtCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken) {
                token.continuePermissionRequest()
            }

        }

        val composite = CompositePermissionListener(permission, dialogPermissionListener)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(composite)
                .check()
    }

    private fun setCameraPermissionHandlerWithSnackbar() {
        val snackbarPermissionListener = SnackbarOnDeniedPermissionListener.Builder
                .with(container, "Camera is needed to take pictures")
                .withOpenSettingsButton("Settings")
                .withCallback(object: Snackbar.Callback() {
                    override fun onShown(sb: Snackbar?) {
                        super.onShown(sb)
                    }

                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }
                })
                .build()


        val permission = object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                setPermissionStatus(txtCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                if (response.isPermanentlyDenied) {
                    setPermissionStatus(txtCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus(txtCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken) {
                token.continuePermissionRequest()
            }

        }

        val composite = CompositePermissionListener(permission, snackbarPermissionListener)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(composite)
                .check()
    }
}