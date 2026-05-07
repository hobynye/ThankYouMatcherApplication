import { useMemo, useState } from "react";
import {
    AlertTriangle,
    CheckCircle,
    Download,
    FileSpreadsheet,
    Loader2,
    UploadCloud
} from "lucide-react";

function FileCard({ title, description, file, onChange, accept }) {
    return (
        <div className="file-card">
            <FileSpreadsheet size={24} />
            <div>
                <h3>{title}</h3>
                <p>{description}</p>

                <label className="secondary-button file-button">
                    Choose file
                    <input
                        type="file"
                        accept={accept}
                        onChange={(event) => onChange(event.target.files?.[0] || null)}
                    />
                </label>

                <div className="selected-file">
                    {file ? file.name : "No file selected"}
                </div>
            </div>
        </div>
    );
}

function StatCard({ label, value, danger }) {
    return (
        <div className={`stat-card ${danger ? "danger" : ""}`}>
            <div>
                <p>{label}</p>
                <h2>{value}</h2>
            </div>
            {danger ? <AlertTriangle /> : <CheckCircle />}
        </div>
    );
}

function buildFormData(studentFile, donorInfoFile, configFile) {
    const formData = new FormData();
    formData.append("studentFile", studentFile);
    formData.append("donorInfoFile", donorInfoFile);
    formData.append("configFile", configFile);
    return formData;
}

export default function App() {
    const [studentFile, setStudentFile] = useState(null);
    const [donorInfoFile, setDonorInfoFile] = useState(null);
    const [configFile, setConfigFile] = useState(null);

    const [assignments, setAssignments] = useState([]);
    const [juniorStaffAssignments, setJuniorStaffAssignments] = useState([]);
    const [errors, setErrors] = useState([]);

    const [backendError, setBackendError] = useState(null);
    const [isPreviewing, setIsPreviewing] = useState(false);
    const [isExporting, setIsExporting] = useState(false);

    const canPreview = studentFile && donorInfoFile && configFile && !isPreviewing;

    const redAlertCount = useMemo(
        () => assignments.filter((assignment) => assignment.redAlert).length,
        [assignments]
    );

    async function previewMatches() {
        setBackendError(null);
        setIsPreviewing(true);

        try {
            const response = await fetch("http://localhost:8080/api/matching/preview", {
                method: "POST",
                body: buildFormData(studentFile, donorInfoFile, configFile)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Unable to preview matches.");
            }

            setAssignments(data.assignments || []);
            setJuniorStaffAssignments(data.juniorStaffAssignments || []);
            setErrors(data.errors || []);
        } catch (error) {
            setAssignments([]);
            setJuniorStaffAssignments([]);
            setErrors([]);
            setBackendError(error.message);
        } finally {
            setIsPreviewing(false);
        }
    }

    return (
        <main className="page">
            <header className="header">
                <div>
                    <p className="eyebrow">HOBY NYE</p>
                    <h1>Thank You Matcher</h1>
                    <p className="subtitle">
                        Upload your student, donor, and configuration files. Preview
                        assignments, review red alerts, then export the final workbook.
                    </p>
                </div>

                <button
                    className="primary-button"
                    disabled={!canPreview || isExporting}
                    onClick={exportExcel}
                >
                    {isExporting ? (
                        <>
                            <Loader2 size={18} className="spin" />
                            Exporting...
                        </>
                    ) : (
                        <>
                            <Download size={18} />
                            Export Excel
                        </>
                    )}
                </button>
            </header>

            <section className="panel">
                <div className="section-title">
                    <UploadCloud />
                    <h2>Input Files</h2>
                </div>

                <div className="file-grid">
                    <FileCard
                        title="Student Info"
                        description="Student names, schools, colors, and groups."
                        file={studentFile}
                        onChange={setStudentFile}
                        accept=".xlsx,.xls"
                    />
                    <FileCard
                        title="Donor Info"
                        description="Donations, gift cards, gift in kind, staff, and speakers."
                        file={donorInfoFile}
                        onChange={setDonorInfoFile}
                        accept=".xlsx,.xls"
                    />
                    <FileCard
                        title="Configuration"
                        description="JSON mapping for sheets, columns, and matching rules."
                        file={configFile}
                        onChange={setConfigFile}
                        accept=".json"
                    />
                </div>

                {backendError && (
                    <div className="backend-error">
                        <AlertTriangle size={20} />
                        <span>{backendError}</span>
                    </div>
                )}

                <div className="actions-row">
                    <p>Files will be validated before matching.</p>
                    <button
                        className="primary-button"
                        disabled={!canPreview}
                        onClick={previewMatches}
                    >
                        {isPreviewing ? (
                            <>
                                <Loader2 size={18} className="spin" />
                                Previewing...
                            </>
                        ) : (
                            "Preview Matches"
                        )}
                    </button>
                </div>
            </section>

            <section className="stats-grid">
                <StatCard label="Assignments" value={assignments.length} />
                <StatCard label="Red Alerts" value={redAlertCount} danger />
                <StatCard label="Validation Messages" value={errors.length} />
                <StatCard label="Junior Staff" value={juniorStaffAssignments.length} />
            </section>

            <section className="content-grid">
                <div className="panel">
                    <h2>Errors & Red Alerts</h2>

                    {errors.length === 0 ? (
                        <p className="empty-state">No validation messages yet.</p>
                    ) : (
                        <div className="error-list scroll-panel">
                            {errors.map((error, index) => (
                                <div className="error-card" key={index}>
                                    <AlertTriangle size={20} />
                                    <div>
                                        <strong>{error.type}</strong>
                                        <p>{error.message}</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className="panel">
                    <h2>Assignments Preview</h2>

                    {assignments.length === 0 ? (
                        <p className="empty-state">No assignments previewed yet.</p>
                    ) : (
                        <div className="table-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>Student</th>
                                <th>Color</th>
                                <th>Group</th>
                                <th>Donor</th>
                                <th>Reason</th>
                                <th>Alert</th>
                            </tr>
                            </thead>
                            <tbody>
                            {assignments.map((assignment, index) => {
                                const student = assignment.student || {};
                                const thankable = assignment.thankable || {};

                                return (
                                    <tr key={`${thankable.id || "assignment"}-${index}`}>
                                        <td>
                                            {student.firstName} {student.lastName}
                                        </td>
                                        <td>{student.color || ""}</td>
                                        <td>{student.group || ""}</td>
                                        <td>
                                            {thankable.orgName ||
                                                thankable.contactName ||
                                                thankable.id ||
                                                "—"}
                                        </td>
                                        <td>{assignment.reason}</td>
                                        <td>
                                            {assignment.redAlert ? (
                                                <span className="badge danger">RED ALERT</span>
                                            ) : (
                                                <span className="badge ok">OK</span>
                                            )}
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                        </div>
                    )}
                </div>
            </section>
        </main>
    );

    async function exportExcel() {
        setBackendError(null);
        setIsExporting(true);

        try {
            const response = await fetch("http://localhost:8080/api/matching/export", {
                method: "POST",
                body: buildFormData(studentFile, donorInfoFile, configFile)
            });

            if (!response.ok) {
                const data = await response.json();
                throw new Error(data.message || "Unable to export workbook.");
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);

            const link = document.createElement("a");
            link.href = url;
            link.download = "thank-you-assignments.xlsx";
            document.body.appendChild(link);
            link.click();
            link.remove();

            window.URL.revokeObjectURL(url);
        } catch (error) {
            setBackendError(error.message);
        } finally {
            setIsExporting(false);
        }
    }
}