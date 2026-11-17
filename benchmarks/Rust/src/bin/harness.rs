use awfy::run::Run;
use std::env::args;

fn process_arguments() -> Run {
    let mut args = args();
    args.next(); // Skip binary name
    let mut run = Run::new(args.next().unwrap());
    if let Some(arg) = args.next() {
        run.set_num_iterations(arg.parse().unwrap());
        if let Some(arg) = args.next() {
            run.set_inner_iterations(arg.parse().unwrap());
        }
    }
    run
}

fn print_usage() {
    println!("Harness [benchmark] [num-iterations [inner-iter]]");
    println!();
    println!("  benchmark      - benchmark class name ");
    println!("  num-iterations - number of times to execute benchmark, default: 1");
    println!("  inner-iter     - number of times the benchmark is executed in an inner loop, ");
    println!("                   which is measured in total, default: 1");
}

fn main() {
    if args().len() < 2 {
        print_usage();
        return;
    }

    let mut run = process_arguments();
    run.run_benchmark();
    run.print_total();
}
