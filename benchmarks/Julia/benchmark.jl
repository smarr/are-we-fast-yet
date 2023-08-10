function benchmark end
function verify_result end

function inner_benchmark_loop(bench, inner_iterations)
    for _ in 1:inner_iterations
        if !verify_result(bench, benchmark(bench))
            return false
        end
    end
    return true
end