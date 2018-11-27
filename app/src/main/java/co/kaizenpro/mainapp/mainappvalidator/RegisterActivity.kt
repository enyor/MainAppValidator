package co.kaizenpro.mainapp.mainappvalidator

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import co.kaizenpro.mainapp.mainappvalidator.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.success
import kotlinx.android.synthetic.main.activity_register.*
import java.util.ArrayList

/**
 * A login screen that offers login via email/password.
 */
class RegisterActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null
    var Users = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Set up the login form.
        //populateAutoComplete()
        //populateComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.importantForAutofill =
                    View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS;
        }




        email_sign_in_button.setOnClickListener { attemptLogin() }

     /*   val mLocMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

       if ( !mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            AlertNoGps();
        }
*/
    }

    fun AlertNoGps() {

        // setup the alert builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("GPS Desactivado")
        builder.setMessage("El sistema GPS esta desactivado y es necesario para la ejecución de la aplicación, ¿Desea activarlo?")

        // add the buttons
        builder.setCancelable(false)
        builder.setPositiveButton("Si", DialogInterface.OnClickListener { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, id -> finish()})

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun populateComplete() {
        if (!mayRequestLocation()) {
            return
        }
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS) })
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    private fun mayRequestLocation(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_ACCESS_FINE_LOCATION ) })
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        /*if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // populateAutoComplete()
            }
        }*/
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateComplete()
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()
        val passconf = confirmar.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        } else if (!isPasswordConfirm(passwordStr, passconf)){
            confirmar.error = getString(R.string.error_password_mismatch)
            focusView = confirmar
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)

            val emailStr = email.text.toString()
            val passwordStr = password.text.toString()
            val nombre = Nombre.text.toString()



            var URL ="https://mainapp.kaizenpro.co.uk/crear_marcador.php?nombre="+nombre+"&correo="+emailStr+"&password="+passwordStr+"&rol=3&enable=1"
            // consulta Ws para ultima posicion del trader
            URL.httpGet().responseObject(Response.Deserializer()) { request, response, result ->
                val (responses, err) = result

                result.success {

                    //Add to ArrayList
                    //responses?.forEach { response ->



                    //}
                  /*  val mLocMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if ( !mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                        AlertNoGps();
                    }*/
                    val intent = Intent(baseContext, MapsActivity::class.java)

                    mAuthTask = null
                    showProgress(false)


                    if (responses!![0].code == 1) {

                        Toast.makeText(this@RegisterActivity, "Usuario Registrado", Toast.LENGTH_LONG).show()
                        finish()

                    } else {

                        Toast.makeText(this@RegisterActivity, responses!![0].text, Toast.LENGTH_LONG).show()
                        //finish()


                    }
/*                    if(Users.isNotEmpty()) {

                        //Validar solo acceso a Rol Clientes
                        if (Users[0].rol.toInt()!=2){
                            email.error = "Acceso permitido solo a Clientes"
                            email.requestFocus()
                        } else if (Users[0].enable.toInt()!=1) {
                            email.error = "El usuario no activo, verifique"
                            email.requestFocus()
                        } else {
                            //Finalmente envia al inicio
                            intent.putExtra("UserId", Users[0].id)
                            intent.putExtra("UserName",Users[0].nombre)
                            startActivity(intent)
                        }
                    }else{
                        password.error = getString(R.string.error_incorrect_password)
                        password.requestFocus()
                    }
*/

                }




            }





            // mAuthTask = UserLoginTask(emailStr, passwordStr)
            // mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    private fun isPasswordConfirm(password: String, confirm: String): Boolean {
        //TODO: Replace this with your own logic
        return password == confirm
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@RegisterActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mEmail: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                return false
            }

            return DUMMY_CREDENTIALS
                    .map { it.split(":") }
                    .firstOrNull { it[0] == mEmail }
                    ?.let {
                        // Account exists, return true if the password matches.
                        it[1] == mPassword
                    }
                    ?: true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null
            showProgress(false)

            if (success!!) {
                //finish()
                val mLocMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if ( !mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                    AlertNoGps();
                }
                val intent = Intent(baseContext, MapsActivity::class.java)
                startActivity(intent)

            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0
        private val REQUEST_ACCESS_FINE_LOCATION = 0

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private val DUMMY_CREDENTIALS = arrayOf("foo@kaizenpro.co.uk:hello", "bar@kaizenpro.co.uk:world", "client1@kaizenpro.co.uk:client")
    }
}