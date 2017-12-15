//
//  main.swift
//  CompilerConstruction
//
//  Created by guxuan on 04/12/2017.
//  Copyright © 2017 gu. All rights reserved.
//

import Foundation

func main() -> Void {
    let argv = Process().arguments

    if argv!.count < 1 {
        return
    }
    
    let token = Token(argv![1])
    
    let parser = Grammar0605Parser(token)
    
    parser.parse()
}

main()





