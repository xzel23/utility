package com.dua3.utility.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import com.dua3.utility.text.MarkDownStyle;
import com.dua3.utility.text.MarkDownTest;
import com.dua3.utility.text.MarkDownUtil;
import com.dua3.utility.text.RichText;

public class SwingUtilTest extends JFrame {

    public static void main(String[] args) throws Exception {
        SwingUtil.setNativeLookAndFeel(SwingUtilTest.class.getSimpleName());

        SwingUtilTest inst = new SwingUtilTest();
        inst.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inst.setSize(new Dimension(800,600));
        inst.setVisible(true);
    }

    public SwingUtilTest() throws Exception {
        super("Swing Utilities test");
        JTabbedPane tabs = new JTabbedPane();
        add(tabs);
        tabs.add("MarkDown Test", new MdTestPanel());
        pack();
    }

    static class MdTestPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private JTextPane mdComponent = new JTextPane();
        private JTextArea sourceComponent = new JTextArea();

        public MdTestPanel() throws Exception {
            setLayout(new BorderLayout());
            String mdSource = MarkDownTest.getTestDataSource();

            JTabbedPane tabs = new JTabbedPane();
            add(tabs, BorderLayout.CENTER);
            tabs.add("preview", new JScrollPane(mdComponent));
            tabs.add("source", new JScrollPane(sourceComponent));

            setSource(mdSource);
        }

        void setSource(String mdSource) {
            sourceComponent.setText(mdSource);
            RichText richtext = MarkDownUtil.convert(mdSource);
            StyledDocument doc = StyledDocumentBuilder.toStyledDocument(richtext, MarkDownStyle.defaultStyles());
            mdComponent.setDocument(doc);
        }
    }
}
