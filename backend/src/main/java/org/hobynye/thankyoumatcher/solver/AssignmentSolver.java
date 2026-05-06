package org.hobynye.thankyoumatcher.solver;

import org.hobynye.thankyoumatcher.model.Assignment;
import org.hobynye.thankyoumatcher.model.Candidate;
import org.hobynye.thankyoumatcher.model.MatchingError;
import org.hobynye.thankyoumatcher.model.MatchingErrorType;
import org.hobynye.thankyoumatcher.model.Student;
import org.hobynye.thankyoumatcher.model.Thankable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssignmentSolver {

    private static final int PREFERRED_ASSIGNMENTS_PER_STUDENT = 2;

    public SolverResult solve(
            Map<Thankable, List<Candidate>> candidateMap,
            List<Student> students
    ) {
        List<Thankable> thankables = new ArrayList<>(candidateMap.keySet());

        int source = 0;
        int thankableStart = 1;
        int studentStart = thankableStart + thankables.size();
        int studentPreferenceStart = studentStart + students.size();
        int sink = studentPreferenceStart + students.size();
        int nodeCount = sink + 1;

        MinCostMaxFlowSolver flowSolver = new MinCostMaxFlowSolver(nodeCount);

        Map<Thankable, Integer> thankableNodeMap = new HashMap<>();
        Map<Student, Integer> studentNodeMap = new HashMap<>();
        Map<String, Candidate> candidateLookup = new HashMap<>();

        int maxStudentCapacity = calculateStudentCapacity(students.size(), thankables.size());

        for (int i = 0; i < thankables.size(); i++) {
            Thankable thankable = thankables.get(i);
            int thankableNode = thankableStart + i;

            thankableNodeMap.put(thankable, thankableNode);
            flowSolver.addEdge(source, thankableNode, 1, 0);
        }

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);

            int studentNode = studentStart + i;
            int preferenceNode = studentPreferenceStart + i;

            studentNodeMap.put(student, studentNode);

            /*
             * First two assignments are cheap.
             * Assignments beyond two are allowed, but more expensive.
             */
            int preferredCapacity = Math.min(
                    PREFERRED_ASSIGNMENTS_PER_STUDENT,
                    maxStudentCapacity
            );

            int extraCapacity = Math.max(
                    0,
                    maxStudentCapacity - preferredCapacity
            );

            flowSolver.addEdge(studentNode, preferenceNode, preferredCapacity, 0);

            if (extraCapacity > 0) {
                flowSolver.addEdge(studentNode, preferenceNode, extraCapacity, 10);
            }

            flowSolver.addEdge(preferenceNode, sink, maxStudentCapacity, 0);
        }

        for (Thankable thankable : thankables) {
            int thankableNode = thankableNodeMap.get(thankable);

            for (Candidate candidate : candidateMap.getOrDefault(thankable, List.of())) {
                Student student = candidate.getStudent();
                int studentNode = studentNodeMap.get(student);

                flowSolver.addEdge(
                        thankableNode,
                        studentNode,
                        1,
                        candidate.getCost()
                );
                candidateLookup.put(key(thankableNode, studentNode), candidate);
            }
        }

        flowSolver.minCostMaxFlow(source, sink);

        List<Assignment> assignments = new ArrayList<>();
        Set<Thankable> matchedThankables = new HashSet<>();

        for (Thankable thankable : thankables) {
            int thankableNode = thankableNodeMap.get(thankable);

            for (CostFlowEdge edge : flowSolver.getEdgesFrom(thankableNode)) {
                if (edge.getFlow() > 0) {
                    Candidate candidate = candidateLookup.get(key(thankableNode, edge.getTo()));

                    if (candidate != null) {
                        Student student = candidate.getStudent();
                        student.setAssignedCount(student.getAssignedCount() + 1);

                        assignments.add(new Assignment(
                                student,
                                thankable,
                                String.join(", ", candidate.getReasons()),
                                candidate.isRedAlert(),
                                candidate.getAlertMessage()
                        ));

                        matchedThankables.add(thankable);
                    }
                }
            }
        }

        List<Thankable> unmatched = thankables.stream()
                .filter(t -> !matchedThankables.contains(t))
                .toList();

        List<MatchingError> errors = unmatched.stream()
                .map(t -> new MatchingError(
                        MatchingErrorType.NO_VALID_STUDENT_MATCH,
                        t.getId(),
                        null,
                        "No valid student could be assigned to thankable " + t.getId()
                ))
                .toList();

        return new SolverResult(assignments, unmatched, errors);
    }

    private int calculateStudentCapacity(int studentCount, int thankableCount) {
        if (studentCount == 0) {
            return 0;
        }

        return (int) Math.ceil((double) thankableCount / studentCount);
    }

    private String key(int thankableNode, int studentNode) {
        return thankableNode + "->" + studentNode;
    }
}