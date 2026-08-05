package com.dateconvertor_java;

import com.nepalidate.ADBS.NepaliDateConverter.AdBs;
import com.nepalidate.ADBS.NepaliDateConverter.NDC;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Native desktop app. No web server, no embedded browser — calls the ADBS
 * library directly, so it starts instantly and has no rendering overhead.
 */
public class DesktopApp extends Application {

    private final NDC ndc = new NDC();
    private final AdBs adBs = new AdBs();

    private ToggleButton bsBtn;
    private ToggleButton adBtn;
    private ToggleGroup modeGroup;
    private TextField yearField;
    private TextField monthField;
    private TextField dayField;
    private Label resultCaption;
    private Label resultValue;
    private Label weekdayValue;
    private Label formatHint;
    private Label error;
    private Button todayBtn;
    private Button copyBtn;
    private VBox resultBox;
    private TextField lastField;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        modeGroup = new ToggleGroup();
        bsBtn = toggle("BS → AD", "Nepali to English");
        adBtn = toggle("AD → BS", "English to Nepali");
        modeGroup.getToggles().addAll(bsBtn, adBtn);
        bsBtn.setSelected(true);

        yearField = field("YYYY", 4);
        monthField = field("MM", 2);
        dayField = field("DD", 2);

        LocalDate today = LocalDate.now();
        String todayBs;
        try {
            todayBs = ndc.adToBs(today.getYear() + "-" + pad(today.getMonthValue()) + "-" + pad(today.getDayOfMonth()));
        } catch (RuntimeException e) {
            todayBs = "";
        }
        Label todayRef = new Label("Today: " + today.getDayOfMonth() + " " + today.getMonth() + " " + today.getYear()
                + (todayBs.isEmpty() ? "" : "  ·  BS " + todayBs));
        todayRef.getStyleClass().add("today-ref");

        Label title = new Label("Date Convertor");
        title.getStyleClass().add("title");
        Label subtitle = new Label("English (AD) ⇄ Nepali (Bikram Sambat)");
        subtitle.getStyleClass().add("subtitle");

        HBox toggleRow = new HBox(6, bsBtn, adBtn);
        toggleRow.getStyleClass().add("mode-toggle");
        HBox.setHgrow(bsBtn, Priority.ALWAYS);
        HBox.setHgrow(adBtn, Priority.ALWAYS);

        HBox fields = new HBox(10,
                labeledField("Year", yearField),
                labeledField("Month", monthField),
                labeledField("Day", dayField));

        todayBtn = new Button("Today");
        todayBtn.getStyleClass().add("today-btn");
        Button convert = new Button("Convert");
        convert.getStyleClass().add("convert-btn");
        formatHint = new Label("Format: YYYY/MM/DD");
        formatHint.getStyleClass().add("hint");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, todayBtn, formatHint, spacer, convert);
        actions.setAlignment(Pos.CENTER_LEFT);

        resultCaption = new Label("English date (AD)");
        resultCaption.getStyleClass().add("result-caption");
        resultValue = new Label();
        resultValue.getStyleClass().add("result-value");
        weekdayValue = new Label();
        weekdayValue.getStyleClass().add("weekday-value");
        copyBtn = new Button("Copy");
        copyBtn.getStyleClass().add("copy-btn");
        resultBox = new VBox(4, resultCaption, resultValue, weekdayValue, copyBtn);
        resultBox.getStyleClass().add("result-box");
        hideResult();

        error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);
        hideError();

        VBox root = new VBox(14, title, subtitle, todayRef, toggleRow, fields, actions, resultBox, error);
        root.setPadding(new Insets(26));
        root.getStyleClass().add("root");

        wireEvents(convert);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/desktop.css").toExternalForm());
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                yearField.clear();
                monthField.clear();
                dayField.clear();
                hideResult();
                hideError();
                yearField.requestFocus();
                e.consume();
            } else if (e.isControlDown() && e.getCode() == KeyCode.C) {
                if (!resultValue.getText().isEmpty()) {
                    copyResult();
                }
                e.consume();
            } else if (e.getCode() == KeyCode.BACK_SPACE && !(scene.getFocusOwner() instanceof TextField)) {
                if (lastField != null) {
                    lastField.positionCaret(lastField.getText().length());
                    lastField.deletePreviousChar();
                }
                e.consume();
            }
        });

        stage.setTitle("Date Convertor");
        this.stage = stage;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(440);
        stage.sizeToScene();
        stage.show();
        yearField.requestFocus();
    }

    private void wireEvents(Button convert) {
        yearField.focusedProperty().addListener((o, ov, f) -> { if (f) lastField = yearField; });
        monthField.focusedProperty().addListener((o, ov, f) -> { if (f) lastField = monthField; });
        dayField.focusedProperty().addListener((o, ov, f) -> { if (f) lastField = dayField; });
        modeGroup.selectedToggleProperty().addListener((o, ov, nv) -> updateMode());
        yearField.textProperty().addListener((o, ov, nv) -> {
            if (nv.length() == 4) monthField.requestFocus();
            clampDay();
        });
        monthField.textProperty().addListener((o, ov, nv) -> {
            int v = intOr(nv, 0);
            if (v > 12) monthField.setText("12");
            if (nv.length() == 2) dayField.requestFocus();
            clampDay();
        });
        dayField.textProperty().addListener((o, ov, nv) -> clampDay());
        yearField.setOnAction(e -> convert());
        monthField.setOnAction(e -> convert());
        dayField.setOnAction(e -> convert());
        convert.setOnAction(e -> convert());
        todayBtn.setOnAction(e -> useToday());
        copyBtn.setOnAction(e -> copyResult());
    }

    private void updateMode() {
        boolean bs = modeGroup.getSelectedToggle() == bsBtn;
        todayBtn.setVisible(!bs);
        formatHint.setText(bs ? "Format: YYYY/MM/DD" : "Format: YYYY-MM-DD");
        resultCaption.setText(bs ? "English date (AD)" : "Nepali date (BS)");
        hideResult();
        clampDay();
    }

    private void useToday() {
        modeGroup.selectToggle(adBtn);
        LocalDate now = LocalDate.now();
        yearField.setText(String.valueOf(now.getYear()));
        monthField.setText(String.valueOf(now.getMonthValue()));
        dayField.setText(String.valueOf(now.getDayOfMonth()));
        hideError();
    }

    private void convert() {
        int y = intOr(yearField.getText(), 0);
        int m = intOr(monthField.getText(), 0);
        int d = intOr(dayField.getText(), 0);
        boolean toAd = modeGroup.getSelectedToggle() == bsBtn;

        if (toAd && (y < 1970 || y > 2100)) { showError("Year must be between 1970 and 2100."); return; }
        if (!toAd && (y < 1913 || y > 2044)) { showError("Year must be between 1913 and 2044."); return; }
        if (m < 1 || m > 12) { showError("Month must be between 1 and 12."); return; }
        int max = maxDay(y, m);
        if (d < 1 || d > max) { showError("Day must be between 1 and " + max + "."); return; }

        try {
            String result;
            LocalDate adDate;
            if (toAd) {
                result = ndc.bsToAd(pad(y) + "/" + pad(m) + "/" + pad(d));
                adDate = LocalDate.parse(result);
            } else {
                result = ndc.adToBs(pad(y) + "-" + pad(m) + "-" + pad(d));
                adDate = LocalDate.of(y, m, d);
            }
            resultValue.setText(result);
            weekdayValue.setText(adDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            hideError();
            showResult();
        } catch (RuntimeException e) {
            showError(e.getMessage() != null ? e.getMessage() : "Invalid date.");
        }
    }

    private void copyResult() {
        ClipboardContent content = new ClipboardContent();
        content.putString(resultValue.getText());
        Clipboard.getSystemClipboard().setContent(content);
        copyBtn.setText("Copied!");
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> copyBtn.setText("Copy"));
        pause.play();
    }

    private int maxDay(int y, int m) {
        if (modeGroup.getSelectedToggle() == adBtn) {
            try {
                return YearMonth.of(y, m).lengthOfMonth();
            } catch (RuntimeException e) {
                return 31;
            }
        }
        for (int d = 32; d >= 1; d--) {
            try {
                if (adBs.validateBsDate(y, m, d)) return d;
            } catch (RuntimeException ignored) {
                // invalid day for this month, try lower
            }
        }
        return 31;
    }

    private void clampDay() {
        int d = intOr(dayField.getText(), 0);
        if (d == 0) return;
        int max = maxDay(intOr(yearField.getText(), 0), intOr(monthField.getText(), 0));
        if (d > max) dayField.setText(String.valueOf(max));
    }

    private void showResult() {
        resultBox.setVisible(true);
        resultBox.setManaged(true);
        stage.sizeToScene();
    }

    private void hideResult() {
        resultBox.setVisible(false);
        resultBox.setManaged(false);
    }

    private void showError(String message) {
        hideResult();
        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
        stage.sizeToScene();
    }

    private void hideError() {
        error.setVisible(false);
        error.setManaged(false);
    }

    private ToggleButton toggle(String caption, String sub) {
        Label cap = new Label(caption);
        cap.getStyleClass().add("toggle-caption");
        Label subLabel = new Label(sub);
        subLabel.getStyleClass().add("toggle-sub");
        VBox box = new VBox(1, cap, subLabel);
        box.setAlignment(Pos.CENTER);
        ToggleButton b = new ToggleButton();
        b.setGraphic(box);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private TextField field(String prompt, int maxLen) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefWidth(78);
        f.setTextFormatter(new TextFormatter<>(c -> {
            String text = c.getControlNewText();
            return text.matches("\\d{0," + maxLen + "}") ? c : null;
        }));
        return f;
    }

    private VBox labeledField(String name, TextField field) {
        Label label = new Label(name);
        label.getStyleClass().add("field-label");
        return new VBox(5, label, field);
    }

    private static int intOr(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException e) {
            return def;
        }
    }

    private static String pad(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }
}
