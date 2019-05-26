package dev.georgekazan.emucab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

	public static JFrame frame;
	public static String romPath;
	public static Map<Type, String> emulators = new HashMap<>();
	public static Map<Type, List<File>> data = new HashMap<>();
	private static DefaultListModel<String> searchModel = new DefaultListModel<String>();
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new FlowLayout());
				
				try {
					getData();
				} catch (IOException e) {
					e.printStackTrace();
				}

				load();
				createMenuBar(frame);
				
				try { 
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
				    e.printStackTrace();
				}
				
				frame.pack();
				frame.setSize(900, 900);
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
	
	private static void getData() throws IOException {
		File file = new File("emulator_cabinet.cfg");
		if(!file.exists()) {
			FileWriter write = new FileWriter(file);
			write.write(JOptionPane.showInputDialog(null, "Enter games folder path"));
			write.write("\n");
			for(Type t : Type.values()) {
				String line = JOptionPane.showInputDialog(null, "Enter " + t.name().toUpperCase() + " emulator path (including .exe)");
				if(line == null || line.isEmpty())
					write.write(".");
				else
					write.write(line);
				write.write("\n");
			}
			write.close();
			getData();
		}
		else {
			Scanner read = new Scanner(file);
			romPath = read.nextLine();
			for(Type t : Type.values()) {
				emulators.put(t, read.nextLine());
			}
			read.close();
		}
	}
	
	private static void load() {
		File file = new File(romPath);
		
		if(!file.exists()) {
			file.mkdirs();
		}
		
		for(File f : file.listFiles()) {
			for(Type t : Type.values()) {
				if(f.getName().endsWith("." + t.getExtension())) {
					if(data.containsKey(t)) {
						data.get(t).add(f);
					}
					else {
						data.put(t, new ArrayList<File>());
						data.get(t).add(f);
					}
				}
			}
		}
		
		{
			JPanel searchPanel = new JPanel();
			searchPanel.setPreferredSize(new Dimension(300, 500));
			searchPanel.setLayout(null);
			searchPanel.setOpaque(true);
			
			JLabel search = new JLabel("Search");
			search.setHorizontalAlignment(SwingConstants.CENTER);
			search.setSize(300, 40);
			search.setFont(new Font("Courier", Font.PLAIN, 44));
			search.setForeground(Color.black);
			
			JTextField searchArea = new JTextField("");
			searchArea.setBounds(0, 45, 300, 25);
			searchPanel.add(search);
			searchPanel.add(searchArea);
	
			searchArea.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					searchModel.clear();
					for(Type t : Type.values()) {
						if(data.get(t) != null) {
							for(File s : data.get(t)) {
								if(s.getName().toLowerCase().contains(searchArea.getText().toLowerCase())) {
									searchModel.addElement(s.getName());
								}
							}
						}
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
				}
			});
			
			JList<String> games = new JList<String>(searchModel);
			games.setVisibleRowCount(4);
			games.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scroll = new JScrollPane(games);
			scroll.setBounds(0, 95, 300, 250);
			searchPanel.add(scroll);
			searchPanel.repaint();
			
			JButton play = new JButton();
			play.setText("Play Game");
			play.setBounds(150 / 2, 370, 150, 40);
			
			play.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Type t = null;
						for(Type a : Type.values()) {
							if(games.getSelectedValue().endsWith(a.getExtension()))
								t = a;
						}
						if(t == null) {
							JOptionPane.showMessageDialog(null, "Unable to find emulator for game " + games.getSelectedValue());
							return;
						}
						new ProcessBuilder(emulators.get(t), romPath + "\\" + games.getSelectedValue()).start();
					}catch(Exception es){
						JOptionPane.showMessageDialog(null, "Couldn't run game or emulator check config!");
						es.printStackTrace();
					}
				}
			});
			searchPanel.add(play);
			frame.add(searchPanel, BorderLayout.NORTH);
		}
		
		for(Type t : Type.values()) {
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(300, 500));
			panel.setLayout(null);
			panel.setOpaque(true);
			
			JLabel label = new JLabel();
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setSize(300, 40);
			label.setText(t.name().toUpperCase());
			label.setFont(new Font("Courier", Font.PLAIN, 44));
			label.setForeground(Color.black);
			
			panel.add(label);
			
			if(data.get(t) != null) {
				String[] dat = new String[data.get(t).size()];
				
				for(int i = 0; i < data.get(t).size(); i++)
					dat[i] = data.get(t).get(i).getName().substring(0, data.get(t).get(i).getName().length() - 4);
				
				JList<String> games = new JList<String>(dat);
				games.setVisibleRowCount(4);
				games.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				JScrollPane scroll = new JScrollPane(games);
				scroll.setBounds(0, 45, 300, 300);
				
				JButton launchGame = new JButton();
				launchGame.setText("Play Game");
				launchGame.setBounds(150 / 2, 370, 150, 40);
				
				launchGame.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							new ProcessBuilder(emulators.get(t), romPath + "\\" + games.getSelectedValue() + "." + t.getExtension()).start();
						}catch(Exception es){
							JOptionPane.showMessageDialog(null, "Couldn't run game or emulator check config!");
							es.printStackTrace();
						}
					}
				});
				
				JButton launchEmu = new JButton();
				launchEmu.setText("Run Emulator");
				launchEmu.setBounds(150 / 2, 420, 150, 40);
				
				launchEmu.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							new ProcessBuilder(emulators.get(t)).start();
						}catch(Exception es){
							JOptionPane.showMessageDialog(null, "Couldn't run emulator!");
							es.printStackTrace();
						}
					}
				});
				
				panel.add(launchEmu);
				panel.add(launchGame);
				panel.add(scroll);
				
				frame.add(panel);
			}
		}
	}
	
	private static void createMenuBar(JFrame frame) {
        JMenuBar menubar = new JMenuBar();
        ImageIcon exit = new ImageIcon(Main.class.getResource("/exit.png"));
        ImageIcon creds = new ImageIcon(Main.class.getResource("/credits.png"));
        ImageIcon helps = new ImageIcon(Main.class.getResource("/help.png"));
        ImageIcon config = new ImageIcon(Main.class.getResource("/settings.png"));
        ImageIcon bugs = new ImageIcon(Main.class.getResource("/bugreport.png"));
        
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        
        JMenu cred = new JMenu("Credits");
        cred.setMnemonic(KeyEvent.VK_C);
        
        JMenu don = new JMenu("Donate");
        don.setMnemonic(KeyEvent.VK_D);
        
        JMenu help = new JMenu("Help");
        cred.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem cMenuItem = new JMenuItem("Credit", creds);
        cMenuItem.setMnemonic(KeyEvent.VK_C);
        cMenuItem.setToolTipText("See Credits");
        cMenuItem.addActionListener((ActionEvent event) -> {
        	JOptionPane.showMessageDialog(null, "Coded by George Kazanjian\n Default background by Knightworm");
        });
        
        JMenuItem helpMen = new JMenuItem("Help", helps);
        helpMen.setMnemonic(KeyEvent.VK_H);
        helpMen.setToolTipText("Help");
        helpMen.addActionListener((ActionEvent event) -> {
        	 	
        });
        
        JMenuItem bugItem = new JMenuItem("Bug Report", bugs);
        bugItem.setMnemonic(KeyEvent.VK_B);
        bugItem.setToolTipText("Report a bug to make the program run better!");
        bugItem.addActionListener((ActionEvent event) -> {
        	
        });

        JMenuItem donMen = new JMenuItem("Donate", creds);
        donMen.setMnemonic(KeyEvent.VK_H);
        donMen.setToolTipText("Donate to support development");
        donMen.addActionListener((ActionEvent event) -> {
        	try {
				Desktop.getDesktop().browse(new URI("https://paypal.me/SoggyMustache"));
			} catch (IOException | URISyntaxException e) {
				JOptionPane.showMessageDialog(null, "Go to \"https://paypal.me/SoggyMustache\" in your web browser");
				e.printStackTrace();
			}
        });
        
        JMenuItem eMenuItem = new JMenuItem("Exit", exit);
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Exit application");
        eMenuItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        
        JMenuItem configItem = new JMenuItem("Configure", config);
        configItem.setMnemonic(KeyEvent.VK_C);
        configItem.setToolTipText("Open Config File");
        configItem.addActionListener((ActionEvent event) -> {
            try {
				Desktop.getDesktop().open(new File("EmuCab.conf"));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Unable to open file");
				e.printStackTrace();
			}
        });

        file.add(eMenuItem);
        file.add(configItem);
        cred.add(cMenuItem);
        help.add(helpMen);
        help.add(bugItem);
        don.add(donMen);
        
        menubar.add(file);
        menubar.add(cred);
        menubar.add(help);
        menubar.add(don);

        frame.setJMenuBar(menubar);
    } 
}
