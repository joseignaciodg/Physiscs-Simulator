package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;

import simulator.control.Controller;
import simulator.model.Body;
import simulator.model.SimulatorObserver;

public class ControlPanel extends JPanel implements SimulatorObserver {
	// ...
	private Controller _ctrl;
	private boolean _stopped;
	private JToolBar _toolBar;
	private JButton _loadButton, _runButton, _stopButton, _quitButton, _gravityButton;
	private JSpinner _stepsSpinner;
	private JTextField _deltaTime;
	private JFileChooser _fileChooser;
	
	ControlPanel(Controller ctrl) {
		_ctrl = ctrl;
		_stopped = true;
		initGUI();
		_ctrl.addObserver(this);
	}
	
	private void initGUI() {
		// TODO build the tool bar by adding buttons, etc.
		setLayout(new BorderLayout());
		_toolBar = new JToolBar();
		add(_toolBar, BorderLayout.PAGE_START);
		
		_fileChooser = new JFileChooser();
		_loadButton = new JButton();
		_loadButton.setToolTipText("Load bodies file into the editor");
		_loadButton.setIcon(loadImage("resources/icons/open.png"));
		_loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFile();
			}
		});
		_toolBar.add(_loadButton);
		_fileChooser.setCurrentDirectory(new File("C:\\Users\\R\\eclipse-workspace\\PhysicsSimulator\\resources"));
		_toolBar.addSeparator();
		
		
		_gravityButton = new JButton();
		_gravityButton.setToolTipText("Select gravity strategy");
		_gravityButton.setIcon(loadImage("resources/icons/physics.png"));
		_gravityButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectGravityStrategy();
			}
		});
		_toolBar.add(_gravityButton);
		_toolBar.addSeparator();
		
		
		_runButton = new JButton();
		_runButton.setToolTipText("Run the simulator");
		_runButton.setIcon(loadImage("resources/icons/run.png"));
		_gravityButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_loadButton.setEnabled(false);
				_runButton.setEnabled(false);
				_quitButton.setEnabled(false);
				_gravityButton.setEnabled(false);
				_stopped = false;
				_ctrl.setDeltaTime(Double.parseDouble(_deltaTime.getText()));
				run_sim((int) _stepsSpinner.getValue());
			}
		});
		_toolBar.add(_runButton);
		
		
		_stopButton = new JButton();
		_stopButton.setToolTipText("Stop the simulator");
		_stopButton.setIcon(loadImage("resources/icons/stop.png"));
		_stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_loadButton.setEnabled(false);
				_runButton.setEnabled(false);
				_stopButton.setEnabled(false);
				_quitButton.setEnabled(false);
				_gravityButton.setEnabled(false);
				_stopped = true;
				run_sim((int) _stepsSpinner.getValue());
			}
		});
		_toolBar.add(_stopButton);
		
		
		_stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 0, 100000, 100));
		_stepsSpinner.setToolTipText("Simulation steps which are executed");
		_stepsSpinner.setMaximumSize(new Dimension(60, 40));
		_stepsSpinner.setMinimumSize(new Dimension(60, 40));
		_stepsSpinner.setPreferredSize(new Dimension(40, 40));
		_toolBar.add(new JLabel(" Steps."));
		_toolBar.add(_stepsSpinner);
		
		
		_deltaTime = new JTextField(5);
		_deltaTime.setToolTipText("Delay between steps in milliseconds");
		_deltaTime.setMaximumSize(new Dimension(70, 70));
		_deltaTime.setMinimumSize(new Dimension(70, 70));
		_toolBar.add(new JLabel(" Delta-Time."));
		_toolBar.add(_deltaTime);
		
		
		_toolBar.add(Box.createGlue());
		_toolBar.addSeparator();
		
		
		_quitButton = new JButton();
		_quitButton.setToolTipText("Exit");
		_quitButton.setIcon(loadImage("resources/icons/exit.png"));
		_quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		_toolBar.add(_quitButton);
	}
	
	// other private/protected methods
	// ...

	protected void selectGravityStrategy() {
		List<JSONObject> _gravityFactoryOptions = _ctrl.getGravityLawsFactory().getInfo();
		String[] options = new String[_gravityFactoryOptions.size()];
		for(int i = 0; i < options.length; ++i) {
			JSONObject info = _gravityFactoryOptions.get(i);
			options[i] = info.getString("desc") + " (" + info.getString("type") + ")";
		}
		
		String str = (String) JOptionPane.showInputDialog(this.getParent(), "Select gavity laws to be used.", "Gravity Laws Selector", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		JSONObject option = null;
		for(int i = 0; i < options.length; ++i) {
			if(str.equals(options[i])) 
				option = new JSONObject(_ctrl.getGravityLawsFactory().getInfo().get(i));
		}
		_ctrl.setGravityLaws(option);
	}
	
	protected void loadFile() {
		int value = _fileChooser.showOpenDialog(this.getParent());
		if(value == JFileChooser.APPROVE_OPTION) {
			File file = _fileChooser.getSelectedFile();
			try {
				_ctrl.reset();
				_ctrl.loadBodies(new FileInputStream(file));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.getParent(), "Something went wrong while loading the file.", "Error loading the file", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	protected ImageIcon loadImage(String direction) {
		return new ImageIcon(direction);
	}
	
	protected void quit() {
		int pane = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(pane == 0) {
			System.exit(0);
		}
	}
	
	protected void setDeltaTime(double dt) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				_deltaTime.setText("" + dt);
			}
		});
	}
	
	private void run_sim(int n) {
		if ( n>0 && !_stopped ) {
			try {
				_ctrl.run(1);
			} catch (Exception e) {
				// TODO show the error in a dialog box
				// TODO enable all buttons
				_loadButton.setEnabled(true);
				_runButton.setEnabled(true);
				_stopButton.setEnabled(true);
				_quitButton.setEnabled(true);
				_gravityButton.setEnabled(true);
				_stopped = true;
				return;
			}
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					run_sim(n-1);
				}
			});
		} else {
			_stopped = true;
			// TODO enable all buttons
			_loadButton.setEnabled(true);
			_runButton.setEnabled(true);
			_stopButton.setEnabled(true);
			_quitButton.setEnabled(true);
			_gravityButton.setEnabled(true);
		}
	}
	// SimulatorObserver methods
	// ...

	@Override
	public void onRegister(List<Body> bodies, double time, double dt, String gLawsDesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReset(List<Body> bodies, double time, double dt, String gLawsDesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBodyAdded(List<Body> bodies, Body b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdvance(List<Body> bodies, double time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeltaTimeChanged(double dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGravityLawChanged(String gLawsDesc) {
		// TODO Auto-generated method stub
		
	}
}