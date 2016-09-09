package rocks.inspectit.ui.rcp.ci.form.part;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.eum.EndUserMonitoringConfig;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;

/**
 * Part for defining the environment general setting.
 *
 * @author Ivan Senic
 *
 */
public class EUMSettingsPart extends SectionPart implements IPropertyListener {

	/**
	 * Form page.
	 */
	private final FormPage formPage;

	/**
	 * Environment being edited.
	 */
	private Environment environment;

	/**
	 * {@link Text} for sending strategy value.
	 */
	private Text scriptBaseUrl;

	/**
	 * Button for class loading delegation.
	 */
	private Button eumEnabledButton;

	private TableViewer tableViewer;

	/**
	 * Default constructor.
	 *
	 * @param formPage
	 *            {@link FormPage} section belongs to.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public EUMSettingsPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createPart(getSection(), toolkit);

		// text and description on our own
		getSection().setText("User Experience Monitoring");
		Label label = toolkit.createLabel(getSection(), "Configuration of the User Experience Monitoring");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(label);
	}

	/**
	 * Creates complete client.
	 *
	 * @param section
	 *            {@link Section}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createPart(Section section, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 10;
		mainComposite.setLayout(gridLayout);
		section.setClient(mainComposite);

		// enable / disable button
		toolkit.createLabel(mainComposite, "Enable User Experience Monitoring:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		eumEnabledButton = toolkit.createButton(mainComposite, "Active", SWT.CHECK);
		eumEnabledButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		eumEnabledButton.setSelection(environment.getEumConfig().isEumEnabled());
		createInfoLabel(mainComposite, toolkit, "If activated, the java agent will inject a script (the JS-Agent) into the webpages and monitor client-side performance data.");

		toolkit.createLabel(mainComposite, "Script Base URL:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		scriptBaseUrl = toolkit.createText(mainComposite, environment.getEumConfig().getScriptBaseUrl(), SWT.BORDER | SWT.RIGHT);
		scriptBaseUrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createInfoLabel(mainComposite, toolkit,
				"The url-prefix under which the agents monitoring scripts will be made accessible to the client.\nThis Url must be mapped by at least one servlet or filter, usually the base-url of other static content like scripts or images is a good choice here.");

		Table table = toolkit.createTable(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);

		GridData tableLayout = new GridData(SWT.FILL, SWT.FILL, true, false);
		tableLayout.horizontalSpan = 2;
		table.setLayoutData(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		createColumns();
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(JSAgentModule.values());
		tableViewer.refresh();
		updateCheckedItems();
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					if (!isDirty()) {
						markDirty();
					}
				}
			}
		});

		// dirty listener
		Listener dirtyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!isDirty()) {
					markDirty();
				}
			}
		};
		eumEnabledButton.addListener(SWT.Selection, dirtyListener);
		scriptBaseUrl.addListener(SWT.Modify, dirtyListener);
	}

	private void createColumns() {
		TableViewerColumn activeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		activeColumn.getColumn().setResizable(false);
		activeColumn.getColumn().setWidth(60);
		activeColumn.getColumn().setText("Active");
		activeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
		});
		activeColumn.getColumn().setToolTipText("Only active modules will be included in the JS Agent and sent to the clients.");

		TableViewerColumn moduleNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		moduleNameColumn.getColumn().setResizable(true);
		moduleNameColumn.getColumn().setWidth(250);
		moduleNameColumn.getColumn().setText("Module");
		moduleNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((JSAgentModule) element).getUiName();
			}

			@Override
			public Image getImage(Object element) {
				// TODO add images to modules;
				return null; // ImageFormatter.getSensorConfigImage((ISensorConfig) element);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getToolTipText(Object element) {
				return ((JSAgentModule) element).getUiDescription();
			}


		});
		moduleNameColumn.getColumn().setToolTipText("Module type.");
	}

	/**
	 * Updates states of the check boxes next to the elements.
	 */
	private void updateCheckedItems() {
		for (TableItem item : tableViewer.getTable().getItems()) {
			JSAgentModule moduleInfo = (JSAgentModule) item.getData();
			EndUserMonitoringConfig eumConfig = environment.getEumConfig();
			item.setChecked(eumConfig.getActiveModules().contains("" + moduleInfo.getIdentifier()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);
			environment.getEumConfig().setEumEnabled(eumEnabledButton.getSelection());
			environment.getEumConfig().setScriptBaseUrl(scriptBaseUrl.getText());
			StringBuilder moduleString = new StringBuilder();
			for (TableItem item : tableViewer.getTable().getItems()) {
				JSAgentModule moduleInfo = (JSAgentModule) item.getData();
				if (item.getChecked()) {
					moduleString.append(moduleInfo.getIdentifier());
				}
			}
			environment.getEumConfig().setActiveModules(moduleString.toString());
			getManagedForm().dirtyStateChanged();
		}
	}

	/**
	 * Creates info icon with given text as tool-tip.
	 *
	 * @param parent
	 *            Composite to create on.
	 * @param toolkit
	 *            {@link FormToolkit} to use.
	 * @param text
	 *            Information text.
	 */
	protected void createInfoLabel(Composite parent, FormToolkit toolkit, String text) {
		Label label = toolkit.createLabel(parent, "");
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
			environment = input.getEnvironment();
		}
	}

	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		super.dispose();
	}

}