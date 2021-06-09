package ru.vsu.cs.skofenko;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import ru.vsu.cs.util.SwingUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Scanner;

public class MainForm extends JFrame {

    private static class SvgPanel extends JPanel {
        private GraphicsNode svgGraphicsNode = null;

        public void paint(String svg) throws IOException {
            String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(xmlParser);
            SVGDocument doc = df.createSVGDocument(null, new StringReader(svg));
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            svgGraphicsNode = builder.build(ctx, doc);

            repaint();
        }

        @Override
        public void paintComponent(Graphics gr) {
            super.paintComponent(gr);

            if (svgGraphicsNode == null) {
                return;
            }

            double scaleX = this.getWidth() / svgGraphicsNode.getPrimitiveBounds().getWidth();
            double scaleY = this.getHeight() / svgGraphicsNode.getPrimitiveBounds().getHeight();
            double scale = Math.min(scaleX, scaleY);
            double deltaX = this.getWidth() - scale * svgGraphicsNode.getPrimitiveBounds().getWidth();
            double deltaY = this.getHeight() - scale * svgGraphicsNode.getPrimitiveBounds().getHeight();
            AffineTransform transform = new AffineTransform(scale, 0, 0, scale, deltaX / 2, deltaY / 2);
            svgGraphicsNode.setTransform(transform);
            Graphics2D g2d = (Graphics2D) gr;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            svgGraphicsNode.paint(g2d);
        }
    }

    private static class DrawPanel extends JPanel {
        static final Color LIGHT_BLUE = new Color(51, 204, 255);
        int mouseX = -1, mouseY = -1;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int i = 0;
            List<WGraphWithCord.Cord> l = paintGraph.getListWithCord();
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            for (int j = 0; j < l.size(); j++) {
                List<WGraph.Pair> edges = paintGraph.getEdges(j);
                for (WGraph.Pair pair : edges) {
                    g.drawLine(l.get(j).x, l.get(j).y, l.get(pair.to).x, l.get(pair.to).y);
                    g.setColor(Color.RED);
                    g.drawString(String.valueOf(pair.l), (l.get(j).x + l.get(pair.to).x) / 2 + 10, (l.get(j).y + l.get(pair.to).y) / 2 - 5);
                    g.setColor(Color.BLACK);
                }
            }
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
            for (WGraphWithCord.Cord cord : l) {
                if (selectedVert == i)
                    g.setColor(Color.RED);
                g.drawOval(cord.x - 20, cord.y - 20, 40, 40);
                if (paintGraph.getMin() == i)
                    g.setColor(LIGHT_BLUE);
                else
                    g.setColor(Color.WHITE);
                g.fillOval(cord.x - 19, cord.y - 19, 38, 38);
                g.setColor(Color.BLACK);
                if (i == 0 || i == 1)
                    g.drawString(String.valueOf(i++), cord.x - 5, cord.y + 7);
                else
                    g.drawString(String.valueOf(i++), cord.x - 5 * (int) Math.ceil(Math.log10(i)), cord.y + 7);
            }
            if (mouseX != -1 && mouseY != -1) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawOval(mouseX - 20, mouseY - 20, 40, 40);
            }
        }

        public void clearCircle() {
            Graphics g = getGraphics();
            g.setColor(Color.WHITE);
            g.drawOval(mouseX - 20, mouseY - 20, 40, 40);
        }
    }

    private JPanel panelMain;
    private JTextArea input;
    private JButton fromFile;
    private JButton toFile;
    private JButton buildButton;
    private JButton findVertButton;
    private JPanel graphCont;
    private JButton vertButton;
    private JButton edgeButton;
    private JPanel drawingPanelCont;
    private JButton noneButton;
    private JButton removeButton;
    private JButton removeAllButton;
    private JButton startButton;

    private final JFileChooser fileChooserOpen;
    private final JFileChooser fileChooserSave;

    private final DrawPanel drawingPanel;
    private final SvgPanel panel;

    private WGraph wGraph = null;
    private static WGraphWithCord paintGraph = new WGraphWithCord(0);

    private final boolean[] state = {false, false, false};
    private static int selectedVert = -1;

    public MainForm() {
        this.setTitle("8 таск");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        fileChooserOpen = new JFileChooser();
        fileChooserOpen.setCurrentDirectory(new File("./tests"));
        fileChooserSave = new JFileChooser();
        fileChooserSave.setCurrentDirectory(new File("./tests"));
        FileFilter txtFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
        fileChooserOpen.setFileFilter(txtFilter);
        fileChooserSave.setFileFilter(txtFilter);
        fileChooserOpen.setAcceptAllFileFilterUsed(false);
        fileChooserOpen.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooserOpen.setApproveButtonText("Open");
        fileChooserSave.setAcceptAllFileFilterUsed(false);
        fileChooserSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserSave.setApproveButtonText("Save");

        graphCont.setLayout(new BorderLayout());
        panel = new SvgPanel();
        graphCont.add(new JScrollPane(panel));

        drawingPanelCont.setLayout(new BorderLayout());
        drawingPanel = new DrawPanel();
        drawingPanel.setBackground(Color.WHITE);
        drawingPanelCont.add(drawingPanel);

        noneButton.setBorder(new LineBorder(Color.RED));

        fromFile.addActionListener(e -> {
            if (fileChooserOpen.showOpenDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
                try (Scanner sc = new Scanner(fileChooserOpen.getSelectedFile())) {
                    sc.useDelimiter("\\Z");
                    input.setText(sc.next());
                } catch (Exception exc) {
                    SwingUtils.showErrorMessageBox(exc);
                }
            }
        });

        toFile.addActionListener(e -> {
            if (fileChooserSave.showSaveDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooserSave.getSelectedFile().getPath();
                if (!filename.toLowerCase().endsWith(".txt")) {
                    filename += ".txt";
                }
                try (FileWriter wr = new FileWriter(filename)) {
                    wr.write(input.getText());
                } catch (Exception exc) {
                    SwingUtils.showErrorMessageBox(exc);
                }
            }
        });
        buildButton.addActionListener(e -> {
            try {
                wGraph = GraphTransformations.buildGraph(input.getText());
                paintGraph(GraphTransformations.toDot(wGraph));
            } catch (Exception exc) {
                SwingUtils.showErrorMessageBox(exc);
            }
        });

        findVertButton.addActionListener(e -> {
            try {
                wGraph.findMinVert();
                paintGraph(GraphTransformations.answerToDot(wGraph));
            } catch (Exception exc) {
                SwingUtils.showErrorMessageBox(exc);
            }
        });

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (state[0]) {
                    if (canPlace(e.getX(), e.getY())) {
                        paintGraph.addVertex(e.getX(), e.getY());
                    }
                } else if (state[1]) {
                    int i = findVertex(e.getX(), e.getY());
                    if (i != -1) {
                        if (selectedVert == -1) {
                            selectedVert = i;
                        } else {
                            if (selectedVert == i) {
                                SwingUtils.showInfoMessageBox("Петли не поддерживаются");
                            } else if (paintGraph.isAdj(selectedVert, i)) {
                                SwingUtils.showInfoMessageBox("Кратные рёбра не поддерживаются");
                            } else {
                                try {
                                    int l = Integer.parseInt(JOptionPane.showInputDialog("Длинна ребра:"));
                                    paintGraph.addEdge(selectedVert, i, l);
                                    selectedVert = -1;
                                } catch (Exception exception) {
                                    SwingUtils.showErrorMessageBox(exception);
                                }
                            }
                        }
                    }

                } else if (state[2]) {
                    int i = findVertex(e.getX(), e.getY());
                    if (i != -1) {
                        paintGraph.removeVert(i);
                    } else {
                        List<WGraphWithCord.Cord> l = paintGraph.getListWithCord();
                        int x = e.getX(), y = e.getY();
                        for (int j = 0; j < l.size(); j++) {
                            List<WGraph.Pair> edges = paintGraph.getEdges(j);
                            int x0 = l.get(j).x;
                            int y0 = l.get(j).y;
                            for (WGraph.Pair pair1 : edges) {
                                int x1 = l.get(pair1.to).x;
                                int y1 = l.get(pair1.to).y;
                                if (Math.abs(x * (y1 - y0) - y * (x1 - x0) - x0 * y1 + y0 * x1) /
                                        Math.sqrt(Math.pow(y1 - y0, 2) + Math.pow(x1 - x0, 2)) < 3) {
                                    paintGraph.removeEdge(j, pair1.to);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    if (selectedVert == -1) {
                        int i = findVertex(e.getX(), e.getY());
                        if (i != -1) {
                            selectedVert = i;
                        }
                    } else {
                        if (canPlace(e.getX(), e.getY())) {
                            paintGraph.getListWithCord().get(selectedVert).x = e.getX();
                            paintGraph.getListWithCord().get(selectedVert).y = e.getY();
                            selectedVert = -1;
                        }
                    }
                }
                drawingPanel.repaint();
            }
        });
        vertButton.addActionListener(e -> buttonClick(vertButton));
        edgeButton.addActionListener(e -> buttonClick(edgeButton));
        removeButton.addActionListener(e -> buttonClick(removeButton));
        removeAllButton.addActionListener(e -> {
            paintGraph = new WGraphWithCord(0);
            selectedVert = -1;
            drawingPanel.repaint();
        });
        noneButton.addActionListener(e -> buttonClick(noneButton));
        startButton.addActionListener(e -> {
            paintGraph.findMinVert();
            drawingPanel.repaint();
        });
        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (state[0]) {
                    drawingPanel.mouseX = e.getX();
                    drawingPanel.mouseY = e.getY();
                    drawingPanel.repaint();
                }
            }
        });
    }

    private void paintGraph(String dot) throws IOException {
        MutableGraph graph = new Parser().read(dot);
        panel.paint(Graphviz.fromGraph(graph).render(Format.SVG).toString());
    }

    private void buttonClick(JButton button) {
        final JButton[] buttons = {vertButton, edgeButton, removeButton, noneButton};
        if (state[0] && button != vertButton) {
            drawingPanel.mouseX = -1; drawingPanel.mouseY = -1;
            drawingPanel.clearCircle();
        }
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == button) {
                buttons[i].setBorder(new LineBorder(Color.RED));
                if (i < 3)
                    state[i] = true;
            } else {
                buttons[i].setBorder(BorderFactory.createEmptyBorder());
                if (i < 3)
                    state[i] = false;
            }
        }
        selectedVert = -1;
        drawingPanel.repaint();
    }

    private boolean canPlace(int x, int y) {
        for (WGraphWithCord.Cord cord : paintGraph.getListWithCord()) {
            if (Math.sqrt(Math.pow(x - cord.x, 2) + Math.pow(y - cord.y, 2)) <= 40) {
                return false;
            }
        }
        return true;
    }

    private int findVertex(int x, int y) {
        int i = 0;
        for (WGraphWithCord.Cord cord : paintGraph.getListWithCord()) {
            if (Math.pow(x - cord.x, 2) + Math.pow(y - cord.y, 2) <= 400) {
                return i;
            }
            i++;
        }
        return -1;
    }
}
