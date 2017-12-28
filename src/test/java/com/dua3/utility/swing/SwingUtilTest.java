package com.dua3.utility.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.utility.text.MarkDownStyle;
import com.dua3.utility.text.MarkDownTest;
import com.dua3.utility.text.MarkDownUtil;
import com.dua3.utility.text.RichText;

public class SwingUtilTest extends JFrame {

	private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(SwingUtilTest.class);

	public static void main(String[] args) throws Exception {
	    LOG.info("setting native look and feel");
        SwingUtil.setNativeLookAndFeel(SwingUtilTest.class.getSimpleName());

        String testfile = args.length == 0 ? "syntax.md" : args[0];
        LOG.info("test file is '{}'", testfile);

        SwingUtilities.invokeLater(()->{
            SwingUtilTest inst = new SwingUtilTest(testfile);
            inst.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            inst.setSize(new Dimension(800,600));
            LOG.info("instance created");
            inst.setVisible(true);
        });
    }

    public SwingUtilTest(String testfile) {
        super("Swing Utilities test");
        JTabbedPane tabs = new JTabbedPane();
        add(tabs);
        tabs.add("MarkDown Test", new MdTestPanel(testfile));
        LOG.info("packing layout");
        pack();
        LOG.info("SwingUtilTest");
    }

    static class MdTestPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private JTextPane mdComponent = new JTextPane();
        private JTextArea sourceComponent = new JTextArea();

        public MdTestPanel(String testfile) {
            setLayout(new BorderLayout());
            String mdSource = MarkDownTest.getTestData(testfile);

            JTabbedPane tabs = new JTabbedPane();
            add(tabs, BorderLayout.CENTER);
            tabs.add("preview", new JScrollPane(mdComponent));
            tabs.add("source", new JScrollPane(sourceComponent));

            setSource(mdSource);
        }

        void setSource(String mdSource) {
            LOG.info("setting source");
            sourceComponent.setText(mdSource);
            LOG.info("creating RichText from source");
            RichText richtext = MarkDownUtil.convert(mdSource);
            LOG.info("converting RichText to StyledDocument");
			DocumentExt doc = StyledDocumentBuilder.toStyledDocument(richtext, MarkDownStyle::getAttributes);
            LOG.info("updating document in UI component");
            doc.setDocumentInto(mdComponent);
            LOG.info("source set");
        }

    }
}
