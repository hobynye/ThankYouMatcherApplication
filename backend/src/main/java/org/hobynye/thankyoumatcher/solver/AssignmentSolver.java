package org.hobynye.thankyoumatcher.solver;

import org.hobynye.thankyoumatcher.model.*;

import java.util.*;

public class AssignmentSolver {

    public SolverResult solve(
            Map<Thankable, List<Candidate>> candidateMap,
            List<Student> students
    ) {
        List<Thankable> thankables = new ArrayList<>(candidateMap.keySet());

        int source = 0;
        int thankableStart = 1;
        int studentStart = thankableStart + thankables.size();
        int sink = studentStart + students.size();
        int nodeCount = sink + 1;

        MaxFlowSolver flowSolver = new MaxFlowSolver(nodeCount);

        Map<Thankable, Integer> thankableNodeMap = new HashMap<>();
        Map<Student, Integer> studentNodeMap = new HashMap<>();
        Map<String, Candidate> candidateLookup = new HashMap<>();

        int studentCapacity = calculateStudentCapacity(students.size(), thankables.size());

        for (int i = 0; i < thankables.size(); i++) {
            Thankable thankable = thankables.get(i);
            int thankableNode = thankableStart + i;
            thankableNodeMap.put(thankable, thankableNode);
            flowSolver.addEdge(source, thankableNode, 1);
        }

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            int studentNode = studentStart + i;
            studentNodeMap.put(student, studentNode);
            flowSolver.addEdge(studentNode, sink, studentCapacity);
        }

        for (Thankable thankable : thankables) {
            int thankableNode = thankableNodeMap.get(thankable);

            for (Candidate candidate : candidateMap.getOrDefault(thankable, List.of())) {
                Student student = candidate.getStudent();
                int studentNode = studentNodeMap.get(student);

                flowSolver.addEdge(thankableNode, studentNode, 1);
                candidateLookup.put(key(thankableNode, studentNode), candidate);
            }
        }

        flowSolver.maxFlow(source, sink);

        List<Assignment> assignments = new ArrayList<>();
        Set<Thankable> matchedThankables = new HashSet<>();

        for (Thankable thankable : thankables) {
            int thankableNode = thankableNodeMap.get(thankable);

            for (FlowEdge edge : flowSolver.getEdgesFrom(thankableNode)) {
                if (edge.getFlow() > 0) {
                    Candidate candidate = candidateLookup.get(key(thankableNode, edge.getTo()));

                    if (candidate != null) {
                        Student student = candidate.getStudent();
                        student.setAssignedCount(student.getAssignedCount() + 1);

                        assignments.add(new Assignment(
                                student,
                                thankable,
                                String.join(", ", candidate.getReasons())
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