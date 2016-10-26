package com.kelsos.mbrc.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.kelsos.mbrc.R
import toothpick.Toothpick

class HelpActivity : AppCompatActivity() {

  @BindView(R.id.toolbar) internal lateinit var mToolbar: Toolbar
  @BindView(R.id.feedback_content) internal lateinit var feedbackEditText: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_feedback)
    ButterKnife.bind(this)
    val scope = Toothpick.openScopes(application, this)
    Toothpick.inject(this, scope)

    setSupportActionBar(mToolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
  }

  override fun onDestroy() {
    super.onDestroy()
    Toothpick.closeScope(this)
  }

  @OnClick(R.id.feedback_button) fun onFeedbackButtonClicked() {
    val feedbackText = feedbackEditText.text.toString().trim { it <= ' ' }
    if (feedbackText.isBlank()) {
      return
    }

    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("kelsos@kelsos.net"))
    emailIntent.type = "message/rfc822"
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
    emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackText)
    startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback_chooser_title)))
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.help, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.action_feedback) {
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
