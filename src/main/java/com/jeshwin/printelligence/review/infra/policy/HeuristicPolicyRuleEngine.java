package com.jeshwin.printelligence.review.infra.policy;

import com.jeshwin.printelligence.review.domain.FindingCategory;
import com.jeshwin.printelligence.review.domain.FindingSeverity;
import com.jeshwin.printelligence.review.domain.PolicyPack;
import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class HeuristicPolicyRuleEngine implements PolicyRuleEngine {

    @Override
    public List<ReviewRecommendation.ReviewFindingDraft> analyze(PullRequestSnapshot snapshot, PolicyPack policyPack) {
        List<ReviewRecommendation.ReviewFindingDraft> findings = new ArrayList<>();
        for (PullRequestSnapshot.ChangedFile file : snapshot.changedFiles()) {
            String patch = file.patch().toLowerCase(Locale.ROOT);
            if (patch.contains("x-internal-auth") && patch.contains("return true")) {
                findings.add(new ReviewRecommendation.ReviewFindingDraft(
                        FindingSeverity.CRITICAL,
                        FindingCategory.SECURITY,
                        file.path(),
                        1,
                        "Authentication bypass path",
                        "The patch appears to allow requests with an internal header to bypass normal authentication checks.",
                        "Remove implicit bypass logic and enforce signed service-to-service authentication or gateway-verified identity."
                ));
            }
            if (patch.contains("logger.info") && patch.contains("request")) {
                findings.add(new ReviewRecommendation.ReviewFindingDraft(
                        FindingSeverity.HIGH,
                        FindingCategory.SECURITY,
                        file.path(),
                        2,
                        "Sensitive request logging",
                        "The controller logs the full request object, which can leak card or PII fields into centralized logs.",
                        "Log request identifiers and coarse metadata only, then mask or omit payment and identity fields."
                ));
            }
            if (patch.contains("cardnumber")) {
                findings.add(new ReviewRecommendation.ReviewFindingDraft(
                        FindingSeverity.HIGH,
                        policyPack == PolicyPack.FINTECH ? FindingCategory.SECURITY : FindingCategory.CODE_SMELL,
                        file.path(),
                        3,
                        "Raw payment data handled in controller",
                        "The controller appears to pass raw card data directly, which couples transport logic to sensitive payment processing.",
                        "Move tokenization or PCI-sensitive handling behind a dedicated payment boundary and keep controllers thin."
                ));
            }
            if (file.additions() > 40 && "java".equalsIgnoreCase(file.language())) {
                findings.add(new ReviewRecommendation.ReviewFindingDraft(
                        FindingSeverity.MEDIUM,
                        FindingCategory.MAINTAINABILITY,
                        file.path(),
                        1,
                        "Large behavioral change in one class",
                        "This file carries a large logic delta, which raises regression risk and review difficulty.",
                        "Split responsibilities into smaller units or add focused tests around the high-risk branches introduced here."
                ));
            }
            if (policyPack == PolicyPack.ZERO_TRUST && patch.contains("bypass")) {
                findings.add(new ReviewRecommendation.ReviewFindingDraft(
                        FindingSeverity.CRITICAL,
                        FindingCategory.ARCHITECTURE,
                        file.path(),
                        1,
                        "Zero-trust policy breach",
                        "The selected policy pack requires explicit trust boundaries, but the patch introduces bypass-style trust semantics.",
                        "Move trust decisions to verifiable identity boundaries and reject any inline bypass mechanism."
                ));
            }
        }
        return findings;
    }
}
