package com.sshtools.twoslices.impl;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.sshtools.twoslices.AbstractToaster;
import com.sshtools.twoslices.ToastType;
import com.sshtools.twoslices.ToasterConfiguration;
import com.sshtools.twoslices.ToasterException;

/**
 * Implementation of a {@link Toaster} when running on Mac OS X with Growl
 * installed. More recent versions of OS X do not have Growl by default, it is a
 * separate (paid) app.
 */
public class OsXToaster extends AbstractToaster {

	private ScriptEngine engine;

	public OsXToaster(ToasterConfiguration configuration) {
		super(configuration);
		try {
			this.configuration = configuration;
			//engine = new ScriptEngineManager().getEngineByName("AppleScript");
			//if(engine == null)
				engine = new ScriptEngineManager().getEngineByName("AppleScriptEngine");

				return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public void toast(ToastType type, String icon, String title, String content) {
		String t = textIcon(type);
		StringBuilder script = new StringBuilder();
		script.append("display notification \"");
		script.append(escape(content));
		script.append("\" with title \"");
		script.append(escape(t.length() == 0 ? title : (t + " " + title)));
		script.append("\"");
		try {
			engine.eval(script.toString(), engine.getContext());
		} catch (ScriptException e) {
			throw new ToasterException(String.format("Failed to show toast for %s: %s", type, title), e);
		}
	}

	private String escape(String text) {
		return text.replace("\"", "\\\"");
	}


}
