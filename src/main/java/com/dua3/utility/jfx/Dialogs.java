package com.dua3.utility.jfx;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public class Dialogs {

	public static class AlertBuilder {
		private AlertBuilder(AlertType type) {
			this.type = type;
		}

		private AlertType type;
		private String title = null;
		private String header = null;
		private String text = null;
		private ButtonType[] buttons;
		private ButtonType defaultButton;

		public Alert build() {
			Alert alert = new Alert(type);

			if (title != null) {
				alert.setTitle(title);
			}

			if (header != null) {
				alert.setHeaderText(header);
			}

			if (text != null) {
				alert.setContentText(text);
			}

			if (buttons != null) {
				alert.getButtonTypes().setAll(buttons);
			}

			if (defaultButton != null) {
				DialogPane pane = alert.getDialogPane();
				for (ButtonType t : alert.getButtonTypes()) {
					((Button) pane.lookupButton(t)).setDefaultButton(t == defaultButton);
				}
			}

			return alert;
		}

		public AlertBuilder title(String fmt, Object... args) {
			this.title = String.format(fmt, args);
			return this;
		}

		public AlertBuilder header(String fmt, Object... args) {
			this.header = String.format(fmt, args);
			return this;
		}

		public AlertBuilder text(String fmt, Object... args) {
			this.text = String.format(fmt, args);
			return this;
		}

		public AlertBuilder buttons(ButtonType... buttons) {
			this.buttons = buttons;
			return this;
		}

		public AlertBuilder defaultButton(ButtonType button) {
			this.defaultButton = button;
			return this;
		}
	}

	public static AlertBuilder alert(AlertType type) {
		return new AlertBuilder(type);
	}

}
