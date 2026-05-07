package org.hobynye.thankyoumatcher.solver;

import org.hobynye.thankyoumatcher.model.*;

import java.util.*;

public class AssignmentSolver {

    private static final int PREFERRED_ASSIGNMENTS_PER_STUDENT = 2;
    private static final int EXTRA_ASSIGNMENT_COST = 10;

    public SolverResult solve(
            Map<Thankable, List<Candidate>> candidateMap,
            List<Student> students
    ) {
        List<Thankable> thankables = new ArrayList<>(candidateMap.keySet());

        int source = 0;
        int thankableStart = 1;

        Map<Student, Integer> studentIndexMap = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            studentIndexMap.put(students.get(i), i);
        }

        Map<StudentOrgKey, Integer> studentOrgPairNodeMap = buildStudentOrgPairNodes(
                candidateMap,
                studentIndexMap,
                thankableStart + thankables.size()
        );

        int pairStart = thankableStart + thankables.size();
        int studentStart = pairStart + studentOrgPairNodeMap.size();
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
                flowSolver.addEdge(studentNode, preferenceNode, extraCapacity, EXTRA_ASSIGNMENT_COST);
            }

            flowSolver.addEdge(preferenceNode, sink, maxStudentCapacity, 0);
        }

        for (Map.Entry<StudentOrgKey, Integer> entry : studentOrgPairNodeMap.entrySet()) {
            StudentOrgKey key = entry.getKey();
            int pairNode = entry.getValue();
            Student student = students.get(key.studentIndex());

            int studentNode = studentNodeMap.get(student);

            /*
             * This enforces:
             * one student should not write more than one letter to the same organization.
             */
            flowSolver.addEdge(pairNode, studentNode, 1, 0);
        }

        for (Thankable thankable : thankables) {
            int thankableNode = thankableNodeMap.get(thankable);

            for (Candidate candidate : candidateMap.getOrDefault(thankable, List.of())) {
                Student student = candidate.getStudent();
                Integer studentIndex = studentIndexMap.get(student);

                if (studentIndex == null) {
                    continue;
                }

                StudentOrgKey pairKey = new StudentOrgKey(
                        studentIndex,
                        donorKey(thankable)
                );

                int pairNode = studentOrgPairNodeMap.get(pairKey);

                flowSolver.addEdge(
                        thankableNode,
                        pairNode,
                        1,
                        candidate.getCost()
                );

                candidateLookup.put(key(thankableNode, pairNode), candidate);
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

    private Map<StudentOrgKey, Integer> buildStudentOrgPairNodes(
            Map<Thankable, List<Candidate>> candidateMap,
            Map<Student, Integer> studentIndexMap,
            int startingNode
    ) {
        Map<StudentOrgKey, Integer> result = new HashMap<>();
        int nextNode = startingNode;

        for (Map.Entry<Thankable, List<Candidate>> entry : candidateMap.entrySet()) {
            Thankable thankable = entry.getKey();
            String donorKey = donorKey(thankable);

            for (Candidate candidate : entry.getValue()) {
                Integer studentIndex = studentIndexMap.get(candidate.getStudent());

                if (studentIndex == null) {
                    continue;
                }

                StudentOrgKey key = new StudentOrgKey(studentIndex, donorKey);

                if (!result.containsKey(key)) {
                    result.put(key, nextNode++);
                }
            }
        }

        return result;
    }

    private int calculateStudentCapacity(int studentCount, int thankableCount) {
        if (studentCount == 0) {
            return 0;
        }

        /*
         * Do not cap students at the average.
         * Earmarked donations may require one matching student to receive
         * a 2nd, 3rd, or later assignment.
         *
         * Balance is handled by cost, not by hard capacity.
         */
        return thankableCount;
    }

    private String donorKey(Thankable thankable) {
        String orgName = normalize(thankable.getOrgName());

        if (!orgName.isBlank()) {
            return "ORG:" + orgName;
        }

        String contactName = normalize(thankable.getContactName());

        if (!contactName.isBlank()) {
            return "CONTACT:" + contactName;
        }

        return "THANKABLE:" + normalize(thankable.getId());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String key(int thankableNode, int pairNode) {
        return thankableNode + "->" + pairNode;
    }

    private record StudentOrgKey(int studentIndex, String donorKey) {
    }
}